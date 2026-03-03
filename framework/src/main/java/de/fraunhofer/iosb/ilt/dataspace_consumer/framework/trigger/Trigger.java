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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.trigger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Trigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Trigger.class);

    protected final DSCExecutor mxPortExecutor;

    /**
     * Base trigger component that provides a simple execute wrapper around the {@link DSCExecutor}.
     * Concrete trigger implementations (scheduler, rest hook) extend this class.
     */
    @Autowired
    public Trigger(DSCExecutor mxPortExecutor) {
        this.mxPortExecutor = mxPortExecutor;
    }

    /**
     * Execute the named MX-Port using the framework executor. Exceptions thrown by the executor are
     * logged but not rethrown to keep triggers resilient.
     *
     * @param mxPortName the name of the MX-Port to execute
     */
    protected void execute(String mxPortName) {
        try {
            LOGGER.info("Trigger invoked for MX-Port: {}", mxPortName);
            mxPortExecutor.execute(mxPortName);
        } catch (DSCExecuteException e) {
            LOGGER.error("Execution failed for MX-Port {}: {}", mxPortName, e.getMessage(), e);
        }
    }
}
