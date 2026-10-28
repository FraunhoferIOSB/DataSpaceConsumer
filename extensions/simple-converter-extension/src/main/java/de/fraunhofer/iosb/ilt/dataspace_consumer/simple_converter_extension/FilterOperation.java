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

/**
 * Defines the filtering operations supported by the simple converter extension.
 *
 * <p>Operations determine how a filter should transform or select data from incoming payloads.
 * Typical usage is to instruct the converter how to extract or combine JSON structures when
 * converting gateway responses into the internal wrapped representation.
 *
 * <ul>
 *   <li>{@link #INDEX_AT} selects an element at a specific zero-based index from an array or list.
 *   <li>{@link #MERGE} merges two JSON objects or arrays into a single JSON structure.
 * </ul>
 */
public enum FilterOperation {

    /**
     * Select the element at the configured zero-based index from an array or list.
     *
     * <p>When this operation is applied the surrounding code is expected to provide an index
     * parameter indicating which element to return. Behavior for out-of-range indexes is determined
     * by the caller of the filter operation.
     */
    INDEX_AT,

    /**
     * Merge two JSON objects or arrays into a single JSON structure.
     *
     * <p>For objects this typically means combining key/value pairs (with caller-defined conflict
     * resolution). For arrays this usually means concatenation. Exact merge semantics are
     * implemented by the converter using this operation.
     */
    MERGE
}
