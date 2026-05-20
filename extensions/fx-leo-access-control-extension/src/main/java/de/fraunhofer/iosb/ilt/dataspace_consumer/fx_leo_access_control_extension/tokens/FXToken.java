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
 * Represents an authentication token returned by the FX-LEO access control service.
 *
 * <p>This record is used to map the JSON token response to a Java type. The JSON properties are
 * bound using Jackson annotations.
 *
 * <p>Both record components correspond to JSON fields emitted by the service. The accessor methods
 * for these components are generated automatically by the Java compiler (they are public and have
 * the same name as the components).
 *
 * @param accessToken the raw access token string (mapped from the JSON property "access_token").
 *     This value is intended to be used in Authorization headers when calling protected endpoints.
 * @param expiresIn the lifetime of the token expressed as a string (mapped from the JSON property
 *     "expires_in"). The exact format depends on the issuing service (commonly seconds or a
 *     duration string).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FXToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") String expiresIn) {}
