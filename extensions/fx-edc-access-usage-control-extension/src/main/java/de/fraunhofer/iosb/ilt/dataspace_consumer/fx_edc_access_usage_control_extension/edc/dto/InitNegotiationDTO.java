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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the EDC response when initializing a contract negotiation.
 *
 * <p>This record contains the essential metadata returned by the EDC when creating (initiating) a
 * negotiation: type information in JSON-LD format, the generated negotiation/resource ID, the
 * creation timestamp, and the associated JSON-LD context.
 *
 * @param type the JSON-LD type of the returned object (e.g., "@type")
 * @param id the ID assigned by the EDC to the started negotiation (e.g., "@id")
 * @param createdAt creation time as a Unix timestamp in milliseconds
 * @param context JSON-LD context with namespace aliases (see {@link ContextDTO})
 */
public record InitNegotiationDTO(
        @JsonProperty("@type") String type,
        @JsonProperty("@id") String id,
        Long createdAt,
        @JsonProperty("@context") ContextDTO context) {}
