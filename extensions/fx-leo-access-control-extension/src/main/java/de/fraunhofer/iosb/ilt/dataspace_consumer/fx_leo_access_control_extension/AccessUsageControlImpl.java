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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.generic.GenericRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.tokens.FXToken;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.tokens.SourceToken;
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
public class AccessUsageControlImpl implements AccessAndUsageControl, Configurable {

    private static final Logger LOGGER = Logger.getLogger(AccessUsageControlImpl.class.getName());

    private CacheService cache;

    private String consumerCredentialServerUrl;
    private String clientId;
    private String clientSecret;
    private String consumerStsUrl;

    private OkHttpClient client;
    private ResponseParser parser;

    private static final String LOG_REQUEST_MSG_FORMAT = "{0} request body: {1}";
    private static final String LOG_RESPONSE_MSG_FORMAT = "{0} response body: {1}";
    private static final String APPLICATION_URL_ENCODE = "application/x-www-form-urlencoded";

    public AccessUsageControlImpl() {
        cache = new CacheService();
        parser = new ResponseParser();
        client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build();
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
        types.add(SubProtocolType.GENERIC);
        return types;
    }

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
                                + response.message()
                                + " - "
                                + response.body());
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
            String bodyString = response.body().string();
            LOGGER.log(
                    Level.FINE,
                    LOG_RESPONSE_MSG_FORMAT,
                    new Object[] {requestName, LoggingUtil.maskToken(bodyString, 10)});
            return bodyString;
        } catch (IOException exception) {
            throw new DSCExecuteException(
                    "IO Exception on "
                            + requestName
                            + " request string: "
                            + exception.getMessage());
        }
    }

    private SourceToken requestSourceToken() {

        String requestName = "sourceToken";
        MediaType mediaType = MediaType.parse(APPLICATION_URL_ENCODE);

        RequestBody body =
                RequestBody.create(
                        String.format(
                                "grant_type=client_credentials&client_id=%s&client_secret=%s",
                                this.clientId, this.clientSecret),
                        mediaType);
        LOGGER.log(Level.FINE, LOG_REQUEST_MSG_FORMAT, new Object[] {requestName, body.toString()});

        Request request =
                new Request.Builder()
                        .url(consumerCredentialServerUrl)
                        .post(body)
                        .addHeader("content-type", APPLICATION_URL_ENCODE)
                        .build();

        Response response = getHttpResponse(request, requestName);

        String responseBodyString = getResponseBodyString(response, requestName);

        SourceToken sourceToken =
                parser.parseJson(
                        () ->
                                parser.getObjectMapper()
                                        .readValue(responseBodyString, SourceToken.class),
                        requestName);

        LOGGER.log(
                Level.FINE,
                "source token: {0}",
                LoggingUtil.maskToken(sourceToken.accessToken(), 5));

        return sourceToken;
    }

    private FXToken requestFXToken(SourceToken sourceToken) {

        String requestName = "fxToken";

        MediaType mediaType = MediaType.parse(APPLICATION_URL_ENCODE);

        RequestBody body =
                RequestBody.create(
                        "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Atoken-exchange&subject_token="
                                + sourceToken.accessToken()
                                + "&subject_token_type=urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Ajwt",
                        mediaType);

        Request request =
                new Request.Builder()
                        .url(this.consumerStsUrl)
                        .post(body)
                        .addHeader("content-type", APPLICATION_URL_ENCODE)
                        .build();

        Response response = getHttpResponse(request, requestName);

        String responseBodyString = getResponseBodyString(response, requestName);

        FXToken fxToken =
                parser.parseJson(
                        () -> parser.getObjectMapper().readValue(responseBodyString, FXToken.class),
                        requestName);

        LOGGER.log(Level.FINE, "fxToken: {0}", LoggingUtil.maskToken(fxToken.accessToken(), 5));

        return fxToken;
    }

    @Override
    public AccessResponse retrieveAccessInformation(AccessRequest accessRequest)
            throws DSCExecuteException {

        if (accessRequest == null) {
            return null; // e.g. discovery needs no authentication
        }
        GenericRequest request = (GenericRequest) accessRequest;
        SourceToken sourceToken = cache.getSourceToken(this.clientId);
        if (sourceToken == null) {
            sourceToken = requestSourceToken();
            cache.putSourceToken(clientId, sourceToken);
        }

        FXToken fxToken = cache.getFXToken(sourceToken);
        if (fxToken == null) {
            fxToken = requestFXToken(sourceToken);
            cache.putFXToken(sourceToken, fxToken);
        }

        return new AccessResponse(request.payload().asText(), fxToken.accessToken(), (Object) null);
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {

        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.consumerCredentialServerUrl = config.get("consumerCredentialServerUrl").toString();
        this.clientId = config.get("clientId").toString();
        this.clientSecret = config.get("clientSecret").toString();
        this.consumerStsUrl = config.get("consumerStsUrl").toString();
    }
}
