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
 * instances implementing {@link Configurable}.
 */
public class DSCComponentConfig {
    private String implementation;
    private Map<String, Object> config;

    /**
     * Returns the fully-qualified implementation class name for the component.
     *
     * @return implementation class name or {@code null} if not configured
     */
    public String getImplementation() {
        return implementation;
    }

    /**
     * Sets the implementation class name for this component.
     *
     * @param implementation fully-qualified class name
     */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /**
     * Returns the configuration map for the component. The map may be {@code null} if no
     * configuration was provided in the application configuration.
     *
     * @return config map or {@code null}
     */
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * Sets the configuration map that will be passed to the plugin instance.
     *
     * @param config map of configuration keys and values
     */
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
}
