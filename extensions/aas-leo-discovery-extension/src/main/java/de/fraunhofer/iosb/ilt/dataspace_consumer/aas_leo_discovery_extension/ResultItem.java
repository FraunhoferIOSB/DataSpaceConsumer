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
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_leo_discovery_extension;

/**
 * Immutable container holding discovery result details for the LEO discovery extension.
 *
 * <p>Instances represent a single discovered endpoint returned by the AAS LEO discovery service.
 * Each record contains the information required to contact or fetch the discovered resource:
 *
 * <ul>
 *   <li>href: HTTP URL that can be used to fetch the asset or submodel content via the Gate.
 *   <li>interfaceType: the interface or protocol identifier for the discovered endpoint (for
 *       example "HTTP", "MQTT" or a vendor-specific interface name).
 * </ul>
 *
 * <p>The record component documentation is exposed as the accessor method documentation in the
 * generated Javadoc (i.e. {@code href()} and {@code interfaceType()}).
 *
 * @param href HTTP href that can be used with GateRequest to retrieve the asset or submodel content
 * @param interfaceType interface type or protocol identifier of the discovered endpoint
 */
public record ResultItem(String href, String interfaceType) {}
