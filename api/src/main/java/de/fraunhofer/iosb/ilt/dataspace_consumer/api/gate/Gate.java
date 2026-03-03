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

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * Gate interface for fetching data in one of the requested serialization formats.
 *
 * <p>Implementations should try to return data in one of the desired formats (in order of
 * preference). If none of the requested formats can be produced, implementations must throw {@link
 * GateFormatNotSupportedException}.
 */
public interface Gate extends ExtensionPoint {
    /**
     * Fetch the data for the given request in one of the specified formats.
     *
     * @param requestInfo request information (url, token)
     * @param desiredFormats desired data response serialization formats (preference order). If
     *     empty, the implementation may return a default format.
     * @return GateResponse containing payload and metadata in one of the desired formats
     * @throws GateFormatNotSupportedException if the Gate cannot provide any of the desired formats
     */
    GateResponse getData(GateRequest requestInfo, List<GateResponseFormat> desiredFormats)
            throws GateFormatNotSupportedException;
}
