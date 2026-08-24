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
package de.fraunhofer.iosb.ilt.dataspace_consumer.json_rest_gate_extension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.pf4j.Extension;

@Extension
public class GateImpl implements Gate, Configurable {

    private String customEndpoint = null;
    private static final Logger LOGGER = Logger.getLogger(GateImpl.class.getName());

    public GateImpl() {}

    @Override
    public GateResponse getData(GateRequest gateRequest, List<GateResponseFormat> desiredFormats) {
        String url = ofNullable(customEndpoint).orElse(gateRequest.url());

        Map<String, List<String>> headers =
                Map.of("token", List.of(gateRequest.token()), "url", List.of(url));

        String body;
        try {
            body = executeRequest(url, gateRequest.token());
        } catch (IOException e) {
            LOGGER.severe(String.format("Could not make http request: %s", e.getMessage()));
            return GateResponse.serverError(GateResponseFormat.JSON, null);
        }

        return GateResponse.success(
                GateResponseFormat.JSON, headers, body.getBytes(StandardCharsets.UTF_8));
    }

    private String executeRequest(String url, String token) throws IOException {
        Request request = new Request.Builder().addHeader("Authorization", token).url(url).build();
        Call httpCall = new OkHttpClient().newCall(request);
        try (Response response = httpCall.execute()) {
            return response.body().string();
        }
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        customEndpoint =
                ofNullable(config).orElse(Map.of()).getOrDefault("endpoint", null).toString();
    }
}
