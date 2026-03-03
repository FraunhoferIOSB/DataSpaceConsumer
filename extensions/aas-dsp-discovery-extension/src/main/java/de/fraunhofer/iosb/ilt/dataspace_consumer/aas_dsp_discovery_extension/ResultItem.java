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
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_discovery_extension;

/**
 * Immutable container holding discovery result details.
 *
 * <p>Instances represent a single discovered asset endpoint returned by the AAS DSP-style catalog.
 * Each record contains the asset identifier, the DSP endpoint that should be used to request access
 * for the asset, and the href that can be used to fetch the actual asset resource via the gate.
 *
 * @param assetId unique identifier of the discovered asset (used in DSP filters)
 * @param endpoint DSP endpoint (URL) to which access requests for this asset should be sent
 * @param href HTTP href that can be used with GateRequest to retrieve the asset content
 */
public record ResultItem(String assetId, String endpoint, String href) {}
