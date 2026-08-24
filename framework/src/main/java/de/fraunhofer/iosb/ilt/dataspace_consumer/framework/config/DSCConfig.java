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

import java.util.Objects;

/**
 * Configuration for a single MX-Port instance.
 *
 * <p>This POJO is used to bind the {@code mx-port} entries from the application configuration (e.g.
 * application.yaml). It contains references to component configurations for each layer (access
 * control, gate, converter and adapter) as well as optional negotiation and trigger settings.
 *
 * @param name the name of this MX-Port.
 * @param discovery the configuration for the discovery component.
 * @param accessAndUsageControl the configuration for the accessAndUsageControl component.
 * @param gate the configuration for the gate component.
 * @param converter the configuration for the converter component.
 * @param adapter the configuration for the adapter component.
 * @param trigger the optional trigger configuration for this MX-Port (resthook, scheduler, ...).
 *     May be null.
 * @param timeout the timeout after which the execution will be aborted.
 * @param synchronous whether this mx port will run synchronously and return its result directly.
 * @param execution the execution configuration for this MX-Port.
 */
public record DSCConfig(
        String name,
        DSCComponentConfig discovery,
        DSCComponentConfig accessAndUsageControl,
        DSCComponentConfig gate,
        DSCComponentConfig converter,
        DSCComponentConfig adapter,
        TriggerConfig trigger,
        Integer timeout,
        Boolean synchronous,
        MxPortExecutionConfig execution) {
    private static final String DEFAULT_NAME = "<unnamed>";
    private static final int DEFAULT_TIMEOUT = 0;
    private static final boolean DEFAULT_SYNCHRONOUS = true;

    public DSCConfig {
        name = Objects.requireNonNullElse(name, DEFAULT_NAME);
        timeout = Objects.requireNonNullElse(timeout, DEFAULT_TIMEOUT);
        synchronous = Objects.requireNonNullElse(synchronous, DEFAULT_SYNCHRONOUS);
    }
}
