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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

/**
 * Container holding an extracted policy JSON string and the associated asset id.
 *
 * <p>This record is used internally to return the raw policy JSON (as a string) along with the
 * asset identifier that the policy targets. The policy string is suitable for inclusion in
 * subsequent negotiation requests sent to the EDC endpoints.
 *
 * @param policy the raw JSON policy string
 * @param assetId the identifier of the asset the policy applies to
 */
public record InitData(String policy, String assetId) {}
