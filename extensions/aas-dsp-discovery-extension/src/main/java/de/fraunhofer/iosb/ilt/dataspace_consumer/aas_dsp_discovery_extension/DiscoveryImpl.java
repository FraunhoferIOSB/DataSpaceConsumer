package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_discovery_extension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery.Discovery;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.pf4j.Extension;

@Extension
/**
 * Discovery implementation that queries an AAS DSP-style catalog and produces discovery and gate
 * access requests for the MX Port framework.
 *
 * <p>This class implements the Discovery interface for JSON payloads (Jackson JsonNode) and is
 * configurable via the Configurable interface. It uses OkHttpClient to perform HTTP requests and
 * Jackson's ObjectMapper to parse JSON responses. The implementation extracts asset identifiers,
 * DSP endpoints and hrefs from the catalogue response and converts them to the appropriate
 * AccessRequest and GateRequest objects used by the framework.
 */
public class DiscoveryImpl implements Discovery<JsonNode>, Configurable {

    private final ObjectMapper mapper;
    private final OkHttpClient client;

    private String baseURL;

    /**
     * Constructs a new DiscoveryImpl instance.
     *
     * <p>The constructor initializes the Jackson ObjectMapper and configures an OkHttpClient with
     * sensible timeouts for connecting, reading, writing and overall call duration.
     */
    public DiscoveryImpl() {
        mapper = new ObjectMapper();
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build();
    }

    private static final Pattern SUBPROTOCOL_PATTERN =
            Pattern.compile("assetId=([^;]+);dsp_endpoint=([^;]+)");

    private void extractFromEndpoints(JsonNode endpoints, List<ResultItem> result) {

        if (!endpoints.isArray()) {
            return;
        }

        for (JsonNode endpoint : endpoints) {
            JsonNode proto = endpoint.path("protocolInformation");

            String href = proto.path("href").asText(null);
            String subBody = proto.path("subprotocolBody").asText(null);

            if (href == null || subBody == null) {
                continue;
            }

            Matcher matcher = SUBPROTOCOL_PATTERN.matcher(subBody);
            if (!matcher.find()) {
                continue;
            }

            String assetId = matcher.group(1);
            String dspEndpoint = matcher.group(2);

            ResultItem item = new ResultItem(assetId, dspEndpoint, href);

            result.add(item);
        }
    }

    private List<ResultItem> getResultItems(JsonNode discoveredInfos) {
        List<ResultItem> result = new ArrayList<>();
        JsonNode assets = discoveredInfos.path("result");
        if (!assets.isArray()) {
            return result;
        }

        for (JsonNode asset : assets) {

            // asset-level endpoints
            extractFromEndpoints(asset.path("endpoints"), result);

            // submodel endpoints
            JsonNode submodels = asset.path("submodelDescriptors");
            if (submodels.isArray()) {
                for (JsonNode submodel : submodels) {
                    extractFromEndpoints(submodel.path("endpoints"), result);
                }
            }
        }

        return result;
    }

    /**
     * Build and return the AccessRequest that should be used to perform the discovery call against
     * the configured catalog service.
     *
     * <p>The request is represented as a DSPRequest with a filter that limits results to Digital
     * Twin Registry entries and targets the configured catalog endpoint (baseURL +
     * "/v3/catalog/request").
     *
     * @return an AccessRequest (DSPRequest) suitable for discovery
     */
    @Override
    public AccessRequest getDiscoveryAccessRequest() {

        DSPFilter filter =
                new DSPFilter(
                        "'http://purl.org/dc/terms/type'.'@id'",
                        null,
                        "https://w3id.org/catenax/taxonomy#DigitalTwinRegistry");
        return new DSPRequest(filter, baseURL + "/v3/catalog/request");
    }

    /**
     * Perform the discovery HTTP request using the provided AccessResponse and parse the response
     * body as JSON.
     *
     * <p>The AccessResponse provides the URL and the authorization token which are used to perform
     * a GET request. A successful HTTP response body is parsed with Jackson and returned as a
     * JsonNode. Any IO or parsing error is wrapped in a RuntimeException.
     *
     * @param accessResponse the AccessResponse containing the discovery URL and authorization token
     * @return the parsed discovery payload as a JsonNode
     * @throws RuntimeException if the HTTP request fails or the response cannot be parsed
     */
    @Override
    public JsonNode discover(AccessResponse accessResponse) {

        Request request =
                new Request.Builder()
                        .url(accessResponse.url())
                        .method("GET", null)
                        .addHeader("Authorization", accessResponse.token())
                        .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " - " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            return mapper.readTree(body.string());

        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch or parse API response", e);
        }
    }

    /**
     * Convert the discovery JSON into a list of AccessRequests that should be sent to the access
     * control service (one per unique assetId).
     *
     * <p>The method extracts result items from the discovery payload and produces a DSPRequest for
     * each distinct assetId. Duplicate assetIds are filtered so only a single AccessRequest per
     * asset is created.
     *
     * @param discoveredInfos the parsed discovery payload
     * @return a list of AccessRequest objects (DSPRequest) targeting DSP endpoints for each
     *     discovered asset
     */
    @Override
    public List<AccessRequest> getGateAccessRequests(JsonNode discoveredInfos) {

        // we only need one access request per assetId
        Set<String> includedAssetIDs = new HashSet<>();

        return getResultItems(discoveredInfos).stream()
                .filter(x -> includedAssetIDs.add(x.assetId()))
                .map(
                        x -> {
                            DSPFilter filter =
                                    new DSPFilter(
                                            "https://w3id.org/edc/v0.0.1/ns/id", null, x.assetId());
                            return (AccessRequest) new DSPRequest(filter, x.endpoint());
                        })
                .toList();
    }

    /**
     * Convert AccessResponses (which contain tokens) and the discovery payload into GateRequest
     * objects used to fetch the actual asset content.
     *
     * <p>This method builds a map from asset identifiers to tokens using the provided
     * accessResponses, then produces a GateRequest for each discovered ResultItem using the
     * discovered href and the corresponding token (may be null if no matching access response
     * exists for a given asset).
     *
     * @param accessResponses the responses from the access control service (contain tokens and
     *     identifiers)
     * @param discoveredInfos the parsed discovery payload
     * @return a list of GateRequest objects containing href and token pairs
     */
    @Override
    public List<GateRequest> convertToGateRequests(
            List<AccessResponse> accessResponses, JsonNode discoveredInfos) {

        // map assetId to token:
        HashMap<String, String> tokenMap = new HashMap<>();
        for (AccessResponse response : accessResponses) {
            tokenMap.put((String) response.identifier(), response.token());
        }

        return getResultItems(discoveredInfos).stream()
                .map(
                        x -> {
                            return new GateRequest(x.href(), tokenMap.get(x.assetId()));
                        })
                .toList();
    }

    /**
     * Configure this discovery implementation.
     *
     * <p>The configuration map must contain a "baseUrl" entry which is used to build the discovery
     * request endpoint. Passing a null configuration will result in an IllegalArgumentException.
     *
     * @param config a map containing configuration values; must include "baseUrl"
     * @throws IllegalArgumentException if the provided config is null
     */
    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.baseURL = config.get("baseUrl").toString();
    }
}
