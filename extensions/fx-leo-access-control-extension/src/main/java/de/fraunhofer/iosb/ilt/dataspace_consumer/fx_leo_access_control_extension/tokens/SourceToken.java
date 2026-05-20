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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.tokens;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data carrier representing the source token returned by the FX LEO access control service.
 *
 * <p>This record is used for JSON deserialization of the authorization response. It maps the JSON
 * properties produced by the authorization endpoint to Java record components using Jackson
 * annotations. The record is immutable and intended solely as a transport representation of the
 * token data.
 *
 * @param accessToken the access token value mapped from the JSON property "access_token"
 * @param expiresIn the expiration information mapped from the JSON property "expires_in"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceToken(
        /**
         * The access token string as returned by the authorization server. This value maps to the
         * JSON property "access_token" and is typically used as a Bearer token in subsequent
         * requests.
         */
        @JsonProperty("access_token") String accessToken,

        /**
         * Expiration information for the token as returned by the authorization server. This maps
         * to the JSON property "expires_in" and contains the expiry information in the format
         * provided by the server (commonly the number of seconds until expiration).
         */
        @JsonProperty("expires_in") String expiresIn) {}
