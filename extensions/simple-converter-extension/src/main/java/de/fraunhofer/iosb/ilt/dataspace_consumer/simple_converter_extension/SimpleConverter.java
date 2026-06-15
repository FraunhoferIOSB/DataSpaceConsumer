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
package de.fraunhofer.iosb.ilt.dataspace_consumer.simple_converter_extension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.Converter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterCapabilities;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import org.pf4j.Extension;

@Extension
/**
 * A simple Converter implementation used for demonstration and testing purposes.
 *
 * <p>Supported operations (configured via {@link #setConfiguration(Map)}):
 *
 * <ul>
 *   <li><b>indexAt</b> - selects a single response by index and returns its JSON payload
 *   <li><b>merge</b> - merges multiple responses into a single wrapped JSON object
 * </ul>
 *
 * The converter advertises support for JSON {@link GateResponseFormat}s.
 */
public class SimpleConverter implements Converter, Configurable {
    private Filter filter;

    /**
     * Explicit public no-argument constructor required by the PF4J extension framework. It is
     * intentionally empty; the framework instantiates extensions via reflection.
     */
    public SimpleConverter() {
        // Intentionally empty: required by the PF4J extension framework which instantiates
        // extensions via reflection. Keeping an explicit no-arg constructor improves clarity
        // for static analysis tools (see SONAR java:S1186).
    }

    /**
     * Convert a list of {@link GateResponse} objects according to the configured filter operation.
     *
     * <p>If the configured operation is {@code indexAt}, the converter returns the JSON payload of
     * the response at the configured index. If the configured operation is {@code merge}, all
     * responses are wrapped into a single JSON object using {@link GateResponsesToWrappedJson}.
     *
     * @param responses the list of gate responses to convert; must not be null
     * @return a {@link ConverterResponse} containing the converted payload and its type
     * @throws UnsupportedOperationException if the configured filter operation is not supported or
     *     if a response has an unsupported format
     * @throws DSCExecuteException if an error occurs during conversion execution
     */
    @Override
    public ConverterResponse convert(List<GateResponse> responses)
            throws UnsupportedOperationException, DSCExecuteException {
        switch (filter.operation()) {
            case FilterOperation.INDEX_AT:
                GateResponse response = getGateResponseOnIndex(responses);
                return new ConverterResponse(ConverterPayloadType.JSON, response.getPayloadBytes());

            case FilterOperation.MERGE:
                GateResponsesToWrappedJson merger = new GateResponsesToWrappedJson();
                return merger.wrapAllAsJsonObject(responses);
            default:
                throw new DSCExecuteException(
                        "Unsupported filter operation: " + filter.operation());
        }
    }

    private GateResponse getGateResponseOnIndex(List<GateResponse> responses) {
        int indexAt;
        try {
            indexAt = (int) filter.value();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Invalid Payload-Typ", e);
        }
        GateResponse response = responses.get(indexAt);
        Optional<GateResponseFormat> format = response.getFormat();
        if (format.isEmpty() || format.get() != GateResponseFormat.JSON) {
            throw new UnsupportedOperationException(
                    "Invalid response format: " + response.getFormat());
        }
        return response;
    }

    /**
     * Returns the converter capabilities advertised by this implementation.
     *
     * @return a {@link ConverterCapabilities} instance declaring supported response formats
     */
    @Override
    public ConverterCapabilities getCapabilities() {
        return new ConverterCapabilities(List.of(GateResponseFormat.JSON));
    }

    /**
     * Configure this converter instance.
     *
     * <p>Expected configuration fields:
     *
     * <ul>
     *   <li><b>operation</b> (String) - either "indexAt" or "merge" to select the filter operation
     *   <li><b>value</b> (Object, optional) - the operation-specific value. For {@code indexAt}
     *       this should be an integer index (0-based) into the responses list
     * </ul>
     *
     * @param config a map containing configuration keys and values; must not be null
     * @throws IllegalArgumentException if the configuration is null, missing required fields, or
     *     contains values of an unexpected type
     */
    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        Object opObj = config.get("operation");
        if (!(opObj instanceof String opStr)) {
            throw new IllegalArgumentException("Configuration field 'operation' must be a String");
        }

        FilterOperation operation;
        switch (opStr) {
            case "indexAt" -> operation = FilterOperation.INDEX_AT;
            case "merge" -> operation = FilterOperation.MERGE;
            default -> throw new IllegalArgumentException("Unknown filter operation: " + opStr);
        }

        Object value = config.get("value");
        this.filter = new Filter(operation, value);
    }
}
