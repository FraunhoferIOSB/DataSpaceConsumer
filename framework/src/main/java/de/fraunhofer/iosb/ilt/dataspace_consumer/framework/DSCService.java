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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import org.springframework.stereotype.Service;

@Service
public class DSCService {
    /** List of configured MX-Port instances injected by Spring. */
    private final List<DSCConfig> mxPorts;

    public DSCService(List<DSCConfig> mxPorts) {
        this.mxPorts = mxPorts;
    }

    /**
     * Returns all configured MX-Ports.
     *
     * @return list of MXPortConfig
     */
    public List<DSCConfig> getMxPorts() {
        return mxPorts;
    }

    /**
     * Find a configured MX-Port by name.
     *
     * @param name the MX-Port name to search for
     * @return the MXPortConfig or {@code null} if not found
     */
    public DSCConfig getPortByName(String name) {
        return mxPorts.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }
}
