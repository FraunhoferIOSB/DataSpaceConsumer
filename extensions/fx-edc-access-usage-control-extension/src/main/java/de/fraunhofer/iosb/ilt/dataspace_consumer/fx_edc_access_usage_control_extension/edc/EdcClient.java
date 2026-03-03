package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.AuthorizationContext;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.InitData;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.LoggingUtil;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.AvailableEdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.EdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.InitNegotiationDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.NegotiationStateDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EdcClient {

    private static final Logger LOGGER = Logger.getLogger(EdcClient.class.getName());
    private static final String LOG_REQUEST_MSG_FORMAT = "{0} request body: {1}";
    private static final String LOG_RESPONSE_MSG_FORMAT = "{0} response body: {1}";

    private static final String APPLICATION_JSON = "application/json";

    private final OkHttpClient client;
    private final String apiKey;
    private final String baseURL;
    private final String counterPartyId;
    private final String counterPartyAddress;

    private EdcResponseParser parser;

    public EdcClient(
            String baseURL, String apiKey, String counterPartyId, String counterPartyAddress) {
        this.apiKey = apiKey;
        this.baseURL = baseURL;
        this.counterPartyAddress = counterPartyAddress;
        this.counterPartyId = counterPartyId;
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build();
        parser = new EdcResponseParser();
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

    public InitData getPolicyFromCatalog(AccessRequest filterRequest) throws DSCExecuteException {

        DSPRequest dspRequest = (DSPRequest) filterRequest;
        DSPFilter filter = dspRequest.getFilters();
        String bodyString =
                EdcRequestTemplates.catalogRequest(
                        counterPartyAddress, counterPartyId, filter.key(), filter.value());

        String loggingName = "policy";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request = getRequest(EdcEndpoints.catalogEndpoint(baseURL), "POST", bodyString);

        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        return parser.policyFromCatalogResponse(responseBodyString);
    }

    public String initiateNegotiation(InitData initData) throws DSCExecuteException {

        String bodyString =
                EdcRequestTemplates.contractNegotiation(counterPartyAddress, initData.policy());

        String loggingName = "negotiation";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request = getRequest(EdcEndpoints.negotiationEndpoint(baseURL), "POST", bodyString);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        InitNegotiationDTO initNegotiationDTO =
                parser.parse(responseBodyString, InitNegotiationDTO.class, "policy");

        LOGGER.log(Level.FINE, "negotiation id: {0}", initNegotiationDTO.id());

        return initNegotiationDTO.id();
    }

    public List<AvailableEdrDTO> getAvailableEDRResponse(String assetId) {
        String bodyString = EdcRequestTemplates.availableEdrQuery(assetId);

        String loggingName = "available EDRs";
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, bodyString});
        Request request =
                getRequest(EdcEndpoints.availableEDRsEndpoint(baseURL), "POST", bodyString);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, responseBodyString});

        return parser.parse(
                responseBodyString, new TypeReference<List<AvailableEdrDTO>>() {}, loggingName);
    }

    public EdrDTO getEDRTokenResponse(String transferProcessId) {

        String loggingName = "token";
        Request request =
                getRequest(EdcEndpoints.tokenEndpoint(baseURL, transferProcessId), "GET", null);
        Response response = getResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {loggingName, LoggingUtil.maskToken(responseBodyString, 10)});
        return parser.parse(responseBodyString, EdrDTO.class, loggingName);
    }

    public boolean isNegotiationFinalized(AuthorizationContext context) {

        if (context.negotiationId() == null) {
            return false;
        }

        String loggingName = "finalized";
        LOGGER.log(Level.FINE, "form status request");
        Request request =
                getRequest(
                        EdcEndpoints.negotiationStatusEndpoint(baseURL, context.negotiationId()),
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
                parser.parse(responseBodyString, NegotiationStateDTO.class, loggingName);
        LOGGER.log(Level.FINE, "state: {0}", negotiationStateDTO.state());

        return negotiationStateDTO.state().equals("FINALIZED");
    }
}
