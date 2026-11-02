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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.http;

import static de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.HttpMethod.GET;
import static de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.HttpMethod.POST;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.HttpMethod;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static final String APPLICATION_JSON = "application/json";
    private static final String X_API_KEY = "X-Api-Key";
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Logger LOGGER = Logger.getLogger(HttpClient.class.getName());

    private final String apiKey;
    private final OkHttpClient client;

    public HttpClient(String apiKey) {
        this.apiKey = apiKey;
        this.client =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .callTimeout(20, TimeUnit.SECONDS)
                        .build();
    }

    /**
     * Execute an HTTP POST request with the given parameters.
     *
     * @param endpoint The HTTP endpoint url.
     * @param body The request body.
     * @return The response body, if the request was successful.
     */
    public String post(String endpoint, String body) {
        return executeRequest(endpoint, body, POST);
    }

    /**
     * Execute an HTTP GET request with the given parameters.
     *
     * @param endpoint The HTTP endpoint url.
     * @return The response body, if the request was successful.
     */
    public String get(String endpoint) {
        return executeRequest(endpoint, null, GET);
    }

    private String executeRequest(String endpoint, String body, HttpMethod httpMethod) {
        String requestId = UUID.randomUUID().toString();
        LOGGER.fine(String.format("Executing request %s: %s %s", requestId, httpMethod, endpoint));
        Request request = buildRequest(endpoint, httpMethod, body);

        String responseBodyString = execute(request);
        LOGGER.fine(String.format("Request %s returned: %s", requestId, responseBodyString));
        return responseBodyString;
    }

    private String execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new DSCExecuteException(
                        String.format(
                                "Request to %s returned HTTP %d - %s",
                                request.url(), response.code(), response.message()));
            }
            return response.body().string();
        } catch (IOException e) {
            throw new DSCExecuteException(
                    String.format("Exception during request to %s", request.url()), e);
        }
    }

    private Request buildRequest(String endpoint, HttpMethod method, String bodyString) {
        Request.Builder builder =
                new Request.Builder()
                        .url(endpoint)
                        .addHeader(X_API_KEY, apiKey)
                        .addHeader(ACCEPT, APPLICATION_JSON);

        if (GET == method) {
            return builder.get().build();
        } else if (bodyString == null) {
            throw new IllegalArgumentException(
                    String.format("%s called without request body", POST));
        }
        return builder.method(
                        method.name(),
                        RequestBody.create(bodyString, MediaType.parse(APPLICATION_JSON)))
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .build();
    }
}
