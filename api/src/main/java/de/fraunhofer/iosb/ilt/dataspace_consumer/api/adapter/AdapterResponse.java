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
package de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;

/**
 * Response from an adapter containing the transformed payload and metadata describing the data
 * format.
 *
 * @param payloadType the payload type
 * @param payload the payload bytes
 * @param headers headers of the gate request
 * @param encoding character encoding (e.g., "UTF-8"), nullable
 */
public record AdapterResponse(
        ConverterPayloadType payloadType,
        byte[] payload,
        Map<String, List<String>> headers,
        String encoding) {
    public AdapterResponse {
        Objects.requireNonNull(payloadType);
        Objects.requireNonNull(payload);
    }

    /**
     * Construct an adapter response from a converter response, copying its fields.
     *
     * @param converterResponse source of this response's fields.
     */
    public AdapterResponse(ConverterResponse converterResponse) {
        this(
                converterResponse.type(),
                converterResponse.payload(),
                ofNullable(converterResponse.headers()).orElse(Map.of()).entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        e -> String.format("source-%s", e.getKey()),
                                        Map.Entry::getValue)),
                converterResponse.encoding());
    }
}
