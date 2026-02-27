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

/** Thrown when a Gate implementation cannot provide data in any of the requested formats. */
public class GateFormatNotSupportedException extends RuntimeException {
    /**
     * Create an exception describing unsupported response formats.
     *
     * @param message human-readable message describing the unsupported formats
     */
    public GateFormatNotSupportedException(String message) {
        super(message);
    }

    /**
     * Create an exception with nested cause information for debugging.
     *
     * @param message human-readable message describing the error
     * @param cause the underlying cause for the failure
     */
    public GateFormatNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
