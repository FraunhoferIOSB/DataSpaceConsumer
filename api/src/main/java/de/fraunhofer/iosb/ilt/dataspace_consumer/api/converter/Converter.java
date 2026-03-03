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

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import org.pf4j.ExtensionPoint;

/**
 * Converter interface for transforming data retrieved by the Gate layer into the internal,
 * application-specific representation consumed by the Adapter layer.
 *
 * <p>This represents layer L2 (semantic model for data exchange). Implementations are expected to
 * declare their supported input formats via {@link ConverterCapabilities}. For example a converter
 * may support JSON, XML, RDF or a binary format such as AASX.
 */
public interface Converter extends ExtensionPoint {

    /**
     * Convert the list of {@link GateResponse} items into a {@link ConverterResponse} for the
     * Adapter layer.
     *
     * <p>Implementations should check each response's format (for example via {@code
     * response.getFormat()}) and either perform the conversion or throw {@link
     * UnsupportedOperationException} when a format is not supported.
     *
     * @param responses the data and format information provided by the Gate
     * @return a {@link ConverterResponse} containing the normalized payload
     * @throws UnsupportedOperationException if any response format is not supported by this
     *     converter
     * @throws DSCExecuteException if an error occurs during conversion
     */
    ConverterResponse convert(List<GateResponse> responses)
            throws UnsupportedOperationException, DSCExecuteException;

    /**
     * Returns the capabilities of this converter which include the set of supported input formats
     * and an optional human-readable description.
     *
     * @return {@link ConverterCapabilities} describing supported formats and metadata
     */
    ConverterCapabilities getCapabilities();
}
