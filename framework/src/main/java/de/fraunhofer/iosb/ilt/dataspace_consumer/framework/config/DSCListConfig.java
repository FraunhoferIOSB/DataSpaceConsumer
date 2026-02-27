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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Spring configuration class that binds the list of configured MX-Ports from the application
 * environment into a {@link List} of {@link DSCConfig}.
 *
 * <p>The bean method reads the top-level property {@code mx-port} and returns an empty list when
 * the property is not present.
 */
@Configuration
public class DSCListConfig {
    @Bean
    public List<DSCConfig> mxPorts(Environment environment) {
        Binder binder = Binder.get(environment);
        return binder.bind("mx-port", Bindable.listOf(DSCConfig.class))
                .orElse(Collections.emptyList());
    }
}
