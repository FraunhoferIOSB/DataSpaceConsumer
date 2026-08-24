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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import org.pf4j.Extension;

/** Get the data by executing an HTTP GET request at the url with the specified access token. */
@Extension
public class GateImpl implements Gate, Configurable {

    private static final Logger LOGGER = Logger.getLogger(GateImpl.class.getName());
    private final HttpClient httpClient;

    private Configuration configuration = null;

    /** Constructor. */
    public GateImpl() {
        this.httpClient = new HttpClient();
    }

    @Override
    public GateResponse getData(GateRequest gateRequest, List<GateResponseFormat> desiredFormats) {
        String url = ofNullable(configuration.endpoint()).orElse(gateRequest.url());

        Map<String, List<String>> headers =
                Map.of("token", List.of(gateRequest.token()), "url", List.of(url));

        try {
            return GateResponse.success(
                    GateResponseFormat.JSON,
                    headers,
                    httpClient.executeRequest(url, gateRequest.token()));
        } catch (IOException e) {
            LOGGER.severe(String.format("HTTP request failed: %s", e.getMessage()));
            return GateResponse.serverError(GateResponseFormat.JSON, e.getMessage().getBytes());
        }
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            return;
        }
        configuration =
                ofNullable(config.get("endpoint"))
                        .map(Object::toString)
                        .map(Configuration::new)
                        .orElse(Configuration.DEFAULT());
    }
}
