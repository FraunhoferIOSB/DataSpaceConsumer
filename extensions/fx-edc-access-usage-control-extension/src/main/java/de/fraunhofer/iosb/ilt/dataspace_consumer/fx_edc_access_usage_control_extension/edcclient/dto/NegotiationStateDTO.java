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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the negotiation state as returned by the EDC.
 *
 * <p>This record maps the EDC's JSON-LD response for negotiation state queries. It contains the
 * JSON-LD type, the current negotiation state (e.g. "REQUESTED", "FINALIZED", ...), and the
 * associated JSON-LD context.
 *
 * @param type JSON-LD type of the object (e.g. "@type")
 * @param state Current state of the negotiation (e.g. "REQUESTED", "FINALIZED")
 * @param context JSON-LD context with namespace aliases (see {@link ContextDTO})
 */
public record NegotiationStateDTO(
        @JsonProperty("@type") String type,
        String state,
        @JsonProperty("@context") ContextDTO context) {}
