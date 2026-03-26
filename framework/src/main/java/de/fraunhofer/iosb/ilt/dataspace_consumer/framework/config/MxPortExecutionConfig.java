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

/**
 * Execution configuration for an MX-Port instance.
 *
 * <p>This POJO contains concurrency and execution-related settings for an MX-Port, such as the
 * maximum number of concurrent gate requests. These settings control how the MX-Port execution
 * behaves at runtime.
 */
public class MxPortExecutionConfig {
    private int maxGateRequests = 10;

    /**
     * Returns the maximum number of concurrent gate requests for this MX-Port.
     *
     * @return the max concurrent gate requests (default: 10)
     */
    public int getMaxGateRequests() {
        return maxGateRequests;
    }

    /**
     * Sets the maximum number of concurrent gate requests for this MX-Port.
     *
     * @param maxGateRequests the max concurrent gate requests
     */
    public void setMaxGateRequests(int maxGateRequests) {
        this.maxGateRequests = maxGateRequests;
    }
}
