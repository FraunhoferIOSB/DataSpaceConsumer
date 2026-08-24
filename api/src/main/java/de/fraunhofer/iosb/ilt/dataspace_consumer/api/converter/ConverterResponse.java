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
package de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Response from a Converter containing the transformed payload and metadata describing the data
 * format.
 *
 * @param type the payload type
 * @param payload the payload bytes
 * @param headers the gate request headers, may be null
 * @param encoding e.g. "UTF-8", "ISO-8859-1", may be null
 * @param schema Optional: Schema-URL or Identifier, may be null
 */
public record ConverterResponse(
        ConverterPayloadType type,
        byte[] payload,
        Map<String, List<String>> headers,
        String encoding,
        String schema) {

    /** Ensure validity of record parameters. */
    public ConverterResponse {
        Objects.requireNonNull(type);
        payload = payload != null ? payload.clone() : new byte[0];
    }

    /**
     * Ensure validity of record parameters.
     *
     * @param type the payload type
     * @param payload the payload bytes
     * @param headers the gate request headers, may be null
     */
    public ConverterResponse(
            ConverterPayloadType type, byte[] payload, Map<String, List<String>> headers) {
        this(type, payload, headers, null, null);
    }

    /**
     * Ensure validity of record parameters.
     *
     * @param type the payload type
     * @param payload the payload bytes
     */
    public ConverterResponse(ConverterPayloadType type, byte[] payload) {
        this(type, payload, null, null, null);
    }
}
