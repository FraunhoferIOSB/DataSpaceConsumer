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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.AuthorizationContext;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.InitData;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.LoggingUtil;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.AvailableEdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.EdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.InitNegotiationDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.NegotiationStateDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EdcClient {

    private String baseURL;
    private String counterPartyId;
    private String counterPartyAddress;
    private String apiKey;
    private OkHttpClient client;
    private final EdcClientParser parser;

    public EdcClient(
            String baseURL,
            String counterPartyId,
            String counterPartyAddress,
            String apiKey,
            OkHttpClient httpClient) {

        this.baseURL = Objects.requireNonNull(baseURL);
        this.counterPartyId = Objects.requireNonNull(counterPartyId);
        this.counterPartyAddress = Objects.requireNonNull(counterPartyAddress);
        this.apiKey = Objects.requireNonNull(apiKey);
        this.client = Objects.requireNonNullElseGet(httpClient, OkHttpClient::new);
        this.parser = new EdcClientParser();
    }

    private static final Logger LOGGER = Logger.getLogger(EdcClient.class.getName());
    private static final String LOG_REQUEST_MSG_FORMAT = "{0} request body: {1}";
    private static final String LOG_RESPONSE_MSG_FORMAT = "{0} response body: {1}";
    private static final String APPLICATION_JSON = "application/json";

    private Response getHttpResponse(Request request, String requestName) {
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new DSCExecuteException(
                        "Request for "
                                + requestName
                                + " ("
                                + request.url()
                                + ") not successful: HTTP "
                                + response.code()
                                + " - "
                                + response.message());
            }
            return response;
        } catch (SocketTimeoutException e) {
            throw new DSCExecuteException(
                    "Timeout during "
                            + requestName
                            + " ("
                            + request.url()
                            + ") request: "
                            + e.getMessage());
        } catch (IOException exception) {
            throw new DSCExecuteException(
                    "Exception on "
                            + requestName
                            + "("
                            + request.url()
                            + ") request execution: "
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

    private Request getHttpRequest(String endpoint, String method, String bodyString) {
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

    private String getResponseString(
            String endpoint,
            String body,
            String httpMethod,
            String loggingName,
            boolean sensitiveLogging) {
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {loggingName, body});
        Request request = getHttpRequest(endpoint, httpMethod, body);

        Response response = getHttpResponse(request, loggingName);
        String responseBodyString = getResponseBodyString(response, loggingName);
        LOGGER.log(
                Level.FINE,
                LOG_RESPONSE_MSG_FORMAT,
                new Object[] {
                    loggingName,
                    sensitiveLogging
                            ? LoggingUtil.maskToken(responseBodyString, 10)
                            : responseBodyString
                });
        return responseBodyString;
    }

    public InitData fetchPolicy(DSPRequest dspRequest) throws DSCExecuteException {

        String loggingName = "policy";
        DSPFilter filter = dspRequest.getFilters();
        String requestBodyString =
                EdcRequestTemplates.catalogRequest(
                        counterPartyAddress, counterPartyId, filter.key(), filter.value());
        String responseBodyString =
                getResponseString(
                        EdcEndpointTemplates.catalogEndpoint(baseURL),
                        requestBodyString,
                        "POST",
                        loggingName,
                        false);
        JsonNode root =
                parser.parseJson(
                        () -> parser.getObjectMapper().readTree(responseBodyString), loggingName);
        return parser.parsePolicy(root);
    }

    public InitNegotiationDTO initiateNegotiation(InitData initData) throws DSCExecuteException {

        String loggingName = "negotiation";
        String requestBodyString =
                EdcRequestTemplates.contractNegotiation(counterPartyAddress, initData.policy());
        String responseBodyString =
                getResponseString(
                        EdcEndpointTemplates.contractNegotiationEndpoint(baseURL),
                        requestBodyString,
                        "POST",
                        loggingName,
                        false);
        InitNegotiationDTO initNegotiationDTO =
                parser.parseJson(
                        () ->
                                parser.getObjectMapper()
                                        .readValue(responseBodyString, InitNegotiationDTO.class),
                        loggingName);

        LOGGER.log(Level.FINE, "negotiation id: {0}", initNegotiationDTO.id());

        return initNegotiationDTO;
    }

    public List<AvailableEdrDTO> getAvailableEDRResponse(String assetId) {
        String loggingName = "available EDRs";
        String requestBodyString = EdcRequestTemplates.availableEdrQuery(assetId);
        String responseBodyString =
                getResponseString(
                        EdcEndpointTemplates.availableEDRsEndpoint(baseURL),
                        requestBodyString,
                        "POST",
                        loggingName,
                        false);
        return parser.parseJson(
                () ->
                        parser.getObjectMapper()
                                .readValue(
                                        responseBodyString,
                                        new TypeReference<List<AvailableEdrDTO>>() {}),
                loggingName);
    }

    public EdrDTO fetchToken(String transferProcessId) {
        String loggingName = "token";
        String responseBodyString =
                getResponseString(
                        EdcEndpointTemplates.tokenEndpoint(baseURL, transferProcessId),
                        null,
                        "GET",
                        loggingName,
                        true);
        return parser.parseJson(
                () -> parser.getObjectMapper().readValue(responseBodyString, EdrDTO.class),
                loggingName);
    }

    public NegotiationStateDTO getNegotiationState(AuthorizationContext context) {
        String loggingName = "negotiation status";
        String responseBodyString =
                getResponseString(
                        EdcEndpointTemplates.negotiationStateEndpoint(
                                baseURL, context.negotiationId()),
                        null,
                        "GET",
                        loggingName,
                        false);
        NegotiationStateDTO negotiationStateDTO =
                parser.parseJson(
                        () ->
                                parser.getObjectMapper()
                                        .readValue(responseBodyString, NegotiationStateDTO.class),
                        loggingName);
        LOGGER.log(Level.FINE, "state: {0}", negotiationStateDTO.state());
        return negotiationStateDTO;
    }
}
