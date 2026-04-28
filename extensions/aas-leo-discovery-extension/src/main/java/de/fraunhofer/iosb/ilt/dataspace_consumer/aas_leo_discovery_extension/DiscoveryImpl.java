/*
 * Copyright (c) 2026 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_leo_discovery_extension;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery.Discovery;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
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
    private OkHttpClient client;
    private DiscoveryRequestFactory factory;
    private static final Logger LOGGER = Logger.getLogger(DiscoveryImpl.class.getName());

    private String baseURL;
    private String x64domain;

    /**
     * Constructs a new DiscoveryImpl instance.
     *
     * <p>The constructor initializes the Jackson ObjectMapper and configures an OkHttpClient with
     * sensible timeouts for connecting, reading, writing and overall call duration.
     */
    public DiscoveryImpl() {
        mapper = new ObjectMapper();
        factory = new DiscoveryRequestFactory();
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .callTimeout(30, TimeUnit.SECONDS)
                        .build();
    }

    /** Leo discovery is without access control */
    @Override
    public AccessRequest getDiscoveryAccessRequest() throws DSCExecuteException {

        return null;
    }

    /**
     * The accessResponse in this discovery call will be ignored, as it is not needed for the
     * request.
     */
    @Override
    public JsonNode discover(AccessResponse accessResponse) throws DSCExecuteException {

        String requestUrl = String.format("%s/companies?domain=%s", this.baseURL, this.x64domain);
        Request request = new Request.Builder().url(requestUrl).method("GET", null).build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " - " + response.message());
            }

            ResponseBody body = response.body();
            String responseBodyString = body.string();
            if (responseBodyString.isBlank()) {
                throw new IOException("Empty response body");
            }

            return mapper.readTree(responseBodyString);

        } catch (IOException e) {

            throw new DSCExecuteException(
                    "Failed to fetch or parse API response from "
                            + requestUrl
                            + ": "
                            + e.getMessage());
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
    public List<AccessRequest> getGateAccessRequests(JsonNode discoveredInfos)
            throws DSCExecuteException {

        List<ResultItem> items = AasDiscoveryParser.getResults(discoveredInfos);
        return factory.getGateAccessRequests(items);
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
            List<AccessResponse> accessResponses, JsonNode discoveredInfos)
            throws DSCExecuteException {

        // map url to token:
        HashMap<String, String> tokenMap = new HashMap<>();
        for (AccessResponse response : accessResponses) {
            tokenMap.put(response.url(), response.token());
        }

        return AasDiscoveryParser.getResults(discoveredInfos).stream()
                .map(
                        x -> {
                            String token = tokenMap.get(x.href());
                            if (token == null) {
                                LOGGER.log(
                                        Level.WARNING,
                                        "No token found for {0}",
                                        new Object[] {x.href()});
                            }
                            LOGGER.log(
                                    Level.FINE,
                                    "Discovery endpoint found: {0}",
                                    new Object[] {x.href()});
                            return new GateRequest(x.href(), token, x.interfaceType());
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

        this.baseURL = config.get("discoveryBaseUrl").toString();
        String domain = config.get("domain").toString();
        this.x64domain = Base64.getEncoder().withoutPadding().encodeToString(domain.getBytes());

        Object timeoutObject = config.get("timeout");
        if (timeoutObject != null) {
            int timeout = Integer.parseInt(timeoutObject.toString());
            this.client =
                    new OkHttpClient.Builder()
                            .connectTimeout(timeout, TimeUnit.SECONDS)
                            .readTimeout(timeout, TimeUnit.SECONDS)
                            .writeTimeout(timeout, TimeUnit.SECONDS)
                            .callTimeout(timeout, TimeUnit.SECONDS)
                            .build();
        }
    }
}
