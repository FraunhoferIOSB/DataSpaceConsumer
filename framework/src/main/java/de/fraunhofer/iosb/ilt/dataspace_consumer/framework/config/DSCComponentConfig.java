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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

import java.util.Map;

/**
 * Configuration holder for a single MX-Port component (e.g. adapter, gate, converter or
 * access-and-usage-control).
 *
 * <p>This POJO maps to the YAML/Properties section describing a component and contains the
 * implementation class name as well as an arbitrary configuration map that will be passed to plugin
 * instances implementing {@link de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable}.
 *
 * @param implementation the fully-qualified implementation class name for the component.
 * @param config the configuration map for the component. The map may be {@code null} if no
 *     configuration was provided in the application configuration.
 */
public record DSCComponentConfig(String implementation, Map<String, Object> config) {}
