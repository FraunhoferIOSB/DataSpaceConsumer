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
package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Response returned by a {@link Gate} containing the payload and metadata.
 *
 * <p>The payload is kept as a byte[]; specific parsers (JSON, XML, RDF, AASX) can be applied by
 * implementations.
 *
 * @param status HTTP-like status code (e.g., 200 for success)
 * @param format optional response format
 * @param headers HTTP headers and metadata
 * @param payload raw payload bytes
 */
public record GateResponse(
        int status, GateResponseFormat format, Map<String, List<String>> headers, byte[] payload) {

    /** Ensures default values of this record. */
    public GateResponse {
        headers = headers != null ? Map.copyOf(headers) : Collections.emptyMap();
        payload = payload != null ? payload.clone() : new byte[0];
    }

    public static GateResponse success(
            GateResponseFormat format, Map<String, List<String>> headers, byte[] payload) {
        return new GateResponse(200, format, headers, payload);
    }

    public static GateResponse serverError(GateResponseFormat format, byte[] reason) {
        return new GateResponse(500, format, null, reason);
    }

    public boolean succeeded() {
        return status >= 200 && status < 300;
    }
}
