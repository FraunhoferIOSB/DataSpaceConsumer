package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPAccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto.AvailableEdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto.EdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto.InitNegotiationDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto.NegotiationStateDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.pf4j.Extension;

@Extension
/**
 * Implementation of DSP-based access and usage control that interacts with a FactoryX / EDC-style
 * policy and negotiation API.
 *
 * <p>This class implements the DSPAccessAndUsageControl interface for AuthorizationContext objects
 * and the Configurable interface. It performs catalog queries, policy extraction, negotiation
 * initiation and token retrieval by calling the configured EDC endpoints using OkHttp and parsing
 * JSON responses with Jackson.
 */
public class AccessUsageControlImpl
        implements DSPAccessAndUsageControl<AuthorizationContext>, Configurable {

    private static final Logger LOGGER = Logger.getLogger(AccessUsageControlImpl.class.getName());
    private static final String LOG_REQUEST_MSG_FORMAT = "{0} request body: {1}";
    private static final String LOG_RESPONSE_MSG_FORMAT = "{0} response body: {1}";

    private static final String APPLICATION_JSON = "application/json";

    private String baseURL;
    private String counterPartyId;
    private String counterPartyAddress;
    private String apiKey;

    private final ObjectMapper mapper;
    private final OkHttpClient client;

    /**
     * Constructs a new AccessUsageControlImpl instance.
     *
     * <p>Initializes the internal JSON mapper and HTTP client used to perform requests to the
     * policy/negotiation endpoints.
     */
    public AccessUsageControlImpl() {
        mapper = new ObjectMapper();
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build();
    }

    private Response getResponse(Request request, String requestName) {
        try {
            return client.newCall(request).execute();
        } catch (SocketTimeoutException e) {
            throw new DSCExecuteException(
                    "Timeout during " + requestName + " request: " + e.getMessage());
        } catch (IOException exception) {
            throw new DSCExecuteException(
                    "Exception on "
                            + requestName
                            + " request execution: "
                            + exception.getMessage());
        }
    }

    private String getResponseBodyString(Response response, String requestName) {
        try {
            return response.body().string();
        } catch (IOException exception) {
            throw new DSCExecuteException(
                    "IO Exception on "
                            + requestName
                            + " request string: "
                            + exception.getMessage());
        }
    }

    @FunctionalInterface
    /**
     * Functional supplier which may throw a JsonProcessingException when called.
     *
     * @param <T> the type of the supplied value
     */
    public interface JsonSupplier<T> {
        T get() throws JsonProcessingException;
    }

    private <T> T parseJson(JsonSupplier<T> parsingOperation, String requestName)
            throws DSCExecuteException {
        try {
            T result = parsingOperation.get();
            LOGGER.log(Level.FINE, "successfully parsed {0} response", requestName);
            return result;
        } catch (JsonProcessingException e) {
            throw new DSCExecuteException(
                    "Exception on " + requestName + " response JSON parsing: : " + e.getMessage(),
                    e);
        }
    }

    private Request getRequest(String endpoint, String method, String bodyString) {
        Request.Builder builder =
                new Request.Builder()
                        .url(endpoint)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("Accept", APPLICATION_JSON);

        if ("GET".equalsIgnoreCase(method)) {
            builder.get();
        } else {
            MediaType mediaType = MediaType.parse(APPLICATION_JSON);
            RequestBody body =
                    bodyString != null
                            ? RequestBody.create(bodyString, mediaType)
                            : RequestBody.create(new byte[0], mediaType);

            builder.method(method, body).addHeader("Content-Type", APPLICATION_JSON);
        }

        return builder.build();
    }

    private InitData getPolicyFromCatalog(AccessRequest filterRequest) throws DSCExecuteException {

        DSPRequest dspRequest = (DSPRequest) filterRequest;

        StringBuilder filterExpression = new StringBuilder();
        filterExpression.append("\"filterExpression\": [\n");

        DSPFilter filter = dspRequest.getFilters();

        filterExpression
                .append("  {\n")
                .append("    \"operandLeft\": \"")
                .append(filter.key())
                .append("\",\n")
                .append("    \"operator\": \"=\",\n")
                .append("    \"operandRight\": \"")
                .append(filter.value())
                .append("\"\n")
                .append("  }");

        filterExpression.append("]");

        String bodyString =
                "{\r\n"
                        + //
                        "  \"@context\": {\r\n"
                        + //
                        "    \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\",\r\n"
                        + //
                        "    \"odrl\": \"http://www.w3.org/ns/odrl/2/\"\r\n"
                        + //
                        "  },\r\n"
                        + //
                        "  \"@type\": \"CatalogRequest\",\r\n"
                        + //
                        "  \"counterPartyAddress\": \""
                        + counterPartyAddress
                        + "\",\r\n"
                        + //
                        "  \"counterPartyId\": \""
                        + counterPartyId
                        + "\",\r\n"
                        + //
                        "  \"protocol\": \"dataspace-protocol-http:2025-1\",\r\n"
                        + //
                        "  \"querySpec\": {\r\n"
                        + //
                        "    \"@type\": \"QuerySpec\",\r\n"
                        + filterExpression
                        + //
                        "    \r\n"
                        + //
                        "  }\r\n"
                        + //
                        "}";

        String loggingName = "policy";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request =
                getRequest(String.format("%s/v3/catalog/request", baseURL), "POST", bodyString);

        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        JsonNode root = parseJson(() -> mapper.readTree(responseBodyString), loggingName);

        JsonNode participantId = root.get("participantId");

        for (JsonNode element : root.get("dataset")) {

            JsonNode idNode = element.get("@id");
            JsonNode id2Node = element.get("id");

            if (idNode != null && idNode.asText().equals(id2Node.asText())) {
                ArrayNode policyNodes = (ArrayNode) element.get("hasPolicy");
                if (policyNodes != null) {
                    ObjectNode policy = (ObjectNode) policyNodes.get(0);

                    ObjectNode target = policy.objectNode();
                    target.set("@id", id2Node.deepCopy());
                    policy.set("odrl:target", target);

                    ObjectNode assigner = policy.objectNode();
                    assigner.set("@id", participantId.deepCopy());
                    policy.set("odrl:assigner", assigner);

                    return new InitData(
                            policy.toString(), id2Node.asText()); // return raw JSON string
                }
            }
        }

        throw new DSCExecuteException("No applicable policy found");
    }

    private String initiateNegotiation(InitData initData) throws DSCExecuteException {

        String bodyString =
                "{\r\n"
                        + //
                        "    \"@context\": [\r\n"
                        + //
                        "        \"https://w3id.org/tractusx/auth/v1.0.0\",\r\n"
                        + //
                        "        \"https://w3id.org/catenax/2025/9/policy/context.jsonld\",\r\n"
                        + //
                        "        \"https://w3id.org/catenax/2025/9/policy/odrl.jsonld\",\r\n"
                        + //
                        "        \"https://w3id.org/dspace/2025/1/context.jsonld\",\r\n"
                        + //
                        "        \"https://w3id.org/edc/dspace/v0.0.1\",\r\n"
                        + //
                        "        {\r\n"
                        + //
                        "            \"fx-policy\": \"https://w3id.org/factoryx/policy/v1.0/\"\r\n"
                        + //
                        "        }\r\n"
                        + //
                        "    ],\r\n"
                        + //
                        "    \"@type\": \"https://w3id.org/edc/v0.0.1/ns/ContractRequest\",\r\n"
                        + //
                        "    \"https://w3id.org/edc/v0.0.1/ns/counterPartyAddress\": \""
                        + counterPartyAddress
                        + "\",\r\n"
                        + //
                        "    \"https://w3id.org/edc/v0.0.1/ns/protocol\":"
                        + " \"dataspace-protocol-http:2025-1\",\r\n"
                        + //
                        "    \"https://w3id.org/edc/v0.0.1/ns/policy\": "
                        + initData.policy()
                        + "\r\n"
                        + //
                        "    }\r\n"
                        + //
                        "}";

        String loggingName = "negotiation";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request = getRequest(String.format("%s/v3/edrs", baseURL), "POST", bodyString);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        InitNegotiationDTO initNegotiationDTO =
                parseJson(
                        () -> mapper.readValue(responseBodyString, InitNegotiationDTO.class),
                        "policy");

        LOGGER.log(Level.FINE, "negotiation id: {0}", initNegotiationDTO.id());

        return initNegotiationDTO.id();
    }

    private List<AvailableEdrDTO> getAvailableEDRResponse(String assetId) {
        String bodyString =
                "{\r\n"
                        + //
                        "    \"@context\": {\r\n"
                        + //
                        "        \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"\r\n"
                        + //
                        "    },\r\n"
                        + //
                        "    \"@type\": \"QuerySpec\",\r\n"
                        + //
                        "    \"sortField\": \"createdAt\",\r\n"
                        + //
                        "    \"sortOrder\": \"DESC\",\r\n"
                        + //
                        "    \"filterExpression\": [\r\n"
                        + //
                        "        {\r\n"
                        + //
                        "            \"operandLeft\": \"assetId\",\r\n"
                        + //
                        "            \"operator\": \"=\",\r\n"
                        + //
                        "            \"operandRight\": \""
                        + assetId
                        + "\"\r\n"
                        + //
                        "        }\r\n"
                        + //
                        "    ]\r\n"
                        + //
                        "}";

        String loggingName = "available EDRs";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request =
                getRequest(String.format("%s/v3/edrs/request", baseURL), "POST", bodyString);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        return parseJson(
                () ->
                        mapper.readValue(
                                responseBodyString, new TypeReference<List<AvailableEdrDTO>>() {}),
                "available EDRs");
    }

    private EdrDTO getEDRTokenResponse(String transferProcessId) {

        String loggingName = "token";
        Request request =
                getRequest(
                        String.format(
                                "%s/v3/edrs/%s/dataaddress?auto_refresh=true",
                                baseURL, transferProcessId),
                        "GET",
                        null);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, maskToken(responseBodyString, 10)});
        return parseJson(() -> mapper.readValue(responseBodyString, EdrDTO.class), "token");
    }

    /**
     * Returns a list with supported subprotocol types.
     *
     * @return a list of supported SubProtocolType values. For this implementation the list contains
     *     SubProtocolType.DSP only.
     */
    @Override
    public List<SubProtocolType> getSupportedSubProtocolTypes() {

        List<SubProtocolType> types = new ArrayList<>();
        types.add(SubProtocolType.DSP);
        return types;
    }

    private Cache<DSPRequest, AuthorizationContext> negotiationCache =
            Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build();

    /**
     * Initiate access negotiation for the provided DSP access request.
     *
     * <p>The method queries the catalog for a matching policy, starts a negotiation and returns an
     * AuthorizationContext containing the negotiation id and the asset id. Any failure during the
     * process throws an DSCExecuteException.
     *
     * @param accessRequest the DSPRequest representing the desired access filters
     * @return an AuthorizationContext containing negotiation id and asset id
     * @throws DSCExecuteException if policy lookup or negotiation fails
     */
    @Override
    public AuthorizationContext initAccess(DSPRequest accessRequest) throws DSCExecuteException {

        AuthorizationContext cached = negotiationCache.getIfPresent(accessRequest);
        if (cached != null) {
            return cached;
        }
        InitData initData = getPolicyFromCatalog(accessRequest);
        String negotiationId = initiateNegotiation(initData);
        LOGGER.log(Level.FINE, "asset id: {0}", initData.assetId());
        LOGGER.log(Level.FINE, "negotiation id: {0}", negotiationId);
        AuthorizationContext context = new AuthorizationContext(negotiationId, initData.assetId());
        negotiationCache.put(accessRequest, context);
        return context;
    }

    /**
     * Check whether the negotiation associated with the given context has reached a finalized
     * state.
     *
     * <p>The method requests the negotiation state from the remote service and returns true when
     * the state equals "FINALIZED". If the negotiation id within the context is null the method
     * returns false.
     *
     * @param context the AuthorizationContext containing the negotiation id
     * @return true if the negotiation state is "FINALIZED", false otherwise
     */
    @Override
    public boolean isNegotiationFinalized(AuthorizationContext context) {

        if (context.negotiationId() == null) {
            return false;
        }

        String loggingName = "finalized";
        LOGGER.log(Level.FINE, "form status request");
        Request request =
                getRequest(
                        String.format(
                                "%s/v3/contractnegotiations/%s/state",
                                baseURL, context.negotiationId()),
                        "GET",
                        null);
        LOGGER.log(Level.FINE, "send status request");
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});
        NegotiationStateDTO negotiationStateDTO =
                parseJson(
                        () -> mapper.readValue(responseBodyString, NegotiationStateDTO.class),
                        loggingName);
        LOGGER.log(Level.FINE, "state: {0}", negotiationStateDTO.state());

        return negotiationStateDTO.state().equals("FINALIZED");
    }

    Cache<AuthorizationContext, EdrDTO> tokenCache =
            Caffeine.newBuilder()
                    .expireAfter(
                            new Expiry<AuthorizationContext, EdrDTO>() {
                                @Override
                                public long expireAfterCreate(
                                        AuthorizationContext key, EdrDTO value, long currentTime) {
                                    if (value.expiresIn() != null) {
                                        long seconds =
                                                Long.parseLong(value.expiresIn())
                                                        - 10; // 10s for all subsequent operations
                                        // until
                                        // token expires
                                        return TimeUnit.SECONDS.toNanos(seconds);
                                    }
                                    return 0; // no expiration info, expire immediately
                                }

                                @Override
                                public long expireAfterUpdate(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }

                                @Override
                                public long expireAfterRead(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }
                            })
                    .build();

    /**
     * Obtain an AccessResponse (endpoint + token) for the given AuthorizationContext.
     *
     * <p>The method looks up available EDRs for the context's asset id, selects the most recent
     * transfer process, fetches the token/data address and returns an AccessResponse containing the
     * endpoint and authorization token. If no available EDRs are found an DSCExecuteException is
     * thrown.
     *
     * @param context the AuthorizationContext containing the asset id
     * @return an AccessResponse with endpoint and token
     * @throws DSCExecuteException if no EDRs are available or token retrieval fails
     */
    @Override
    public AccessResponse getTokenForAccess(AuthorizationContext context)
            throws DSCExecuteException {

        EdrDTO cached = tokenCache.getIfPresent(context);
        if (cached != null) {
            return new AccessResponse(cached.endpoint(), cached.authorization(), context.assetId());
        }

        AvailableEdrDTO edr;
        try {
            edr = getAvailableEDRResponse(context.assetId()).getFirst();
        } catch (NoSuchElementException exception) {
            throw new DSCExecuteException("No available EDRs found. No token available");
        }
        String newestTransferProcessID = edr.transferProcessId();
        EdrDTO dto = getEDRTokenResponse(newestTransferProcessID);
        tokenCache.put(context, dto);
        LOGGER.log(Level.FINE, "endpoint: {0}", dto.endpoint());
        LOGGER.log(Level.FINE, "token: {0}", maskToken(dto.authorization(), 4));

        return new AccessResponse(dto.endpoint(), dto.authorization(), context.assetId());
    }

    /**
     * Masks the central part of a token string leaving the specified number of characters visible
     * at both ends.
     *
     * @param token the token string to mask
     * @param visible number of characters to keep visible at each end
     * @return the masked token string
     */
    private static String maskToken(String token, int visible) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        if (token.length() <= visible * 2) {
            return "*".repeat(token.length());
        }

        String start = token.substring(0, visible);
        String end = token.substring(token.length() - visible);
        String masked = "*".repeat(token.length() - (visible * 2));

        return start + masked + end;
    }

    /**
     * Configure the EDC client parameters used by this implementation.
     *
     * <p>The provided configuration map must contain the keys "baseUrl", "apiKey",
     * "counterPartyAddress" and "counterPartyId". Passing a null configuration will result in an
     * IllegalArgumentException.
     *
     * @param config configuration map containing required entries
     * @throws IllegalArgumentException if config is null
     */
    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {

        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.baseURL = config.get("baseUrl").toString();
        this.apiKey = config.get("apiKey").toString();
        this.counterPartyAddress = config.get("counterPartyAddress").toString();
        this.counterPartyId = config.get("counterPartyId").toString();
    }
}
