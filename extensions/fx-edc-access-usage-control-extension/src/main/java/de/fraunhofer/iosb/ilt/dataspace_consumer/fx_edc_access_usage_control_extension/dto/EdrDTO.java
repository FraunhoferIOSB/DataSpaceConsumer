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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the DataAddress / token information returned by the EDC for an Endpoint Data Reference
 * (EDR).
 *
 * <p>This record is mapped from the EDC JSON response and contains both metadata (e.g. RDF/JSON-LD
 * type information) and the endpoint and authentication details required to access the asset.
 *
 * @param objectType RDF/JSON-LD type (@type) of the object
 * @param type Type field (free text depending on the EDC response)
 * @param flowType Indicates the data transfer flow (e.g. "STREAM" or "BATCH")
 * @param endpointType Type of the endpoint (e.g. "HttpProxy")
 * @param refreshEndpoint Endpoint to which refresh requests for the token can be sent
 * @param transferTypeDestination Destination type of the transfer (transferTypeDestination)
 * @param audience Audience/target for the token (tx-auth:audience)
 * @param endpoint URL of the actual data endpoint
 * @param refreshToken Optional refresh token (tx-auth:refreshToken)
 * @param expiresIn Token time-to-live / expiry (tx-auth:expiresIn)
 * @param authorization Content to use for the Authorization header when calling the endpoint
 * @param refreshAudience Audience used for refresh token actions (tx-auth:refreshAudience)
 * @param context JSON-LD context information (see {@link ContextDTO})
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EdrDTO(
        @JsonProperty("@type") String objectType,
        String type,
        String flowType,
        String endpointType,
        @JsonProperty("tx-auth:refreshEndpoint") String refreshEndpoint,
        String transferTypeDestination,
        @JsonProperty("tx-auth:audience") String audience,
        String endpoint,
        @JsonProperty("tx-auth:refreshToken") String refreshToken,
        @JsonProperty("expiresIn")
                @JsonAlias({
                    "tx-auth:expiresIn",
                    "expiresIn",
                    "https://w3id.org/tractusx/auth/expiresIn"
                })
                String expiresIn,
        String authorization,
        @JsonProperty("tx-auth:refreshAudience") String refreshAudience,
        @JsonProperty("@context") ContextDTO context) {}
