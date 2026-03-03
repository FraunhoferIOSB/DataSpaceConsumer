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
package de.fraunhofer.iosb.ilt.dataspace_consumer.console_adapter_extension;

import java.nio.charset.StandardCharsets;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
/**
 * Simple adapter implementation that logs incoming converter payloads to the application logger
 * (console). This adapter is intended for debugging and inspection purposes: it prints the payload
 * type and the UTF-8 decoded payload content at INFO level.
 */
public class ConsoleAdapter implements Adapter {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleAdapter.class);

    /**
     * Creates a new ConsoleAdapter instance.
     *
     * <p>No special initialization is required for this adapter.
     */
    public ConsoleAdapter() {}

    /**
     * Logs the payload contained in the given ConverterResponse to the console.
     *
     * <p>If the provided request or its payload is null, a warning is logged and the method returns
     * without throwing an exception. Otherwise the payload type and the UTF-8 decoded payload
     * contents are logged at INFO level.
     *
     * @param request the ConverterResponse containing the payload to log
     * @throws DSCExecuteException declared by the Adapter interface; this implementation does not
     *     throw it under normal circumstances
     */
    @Override
    public void adapt(ConverterResponse request) throws DSCExecuteException {

        if (request == null || request.getPayload() == null) {
            LOG.warn("ConverterResponse oder Payload ist null");
            return;
        }

        byte[] payload = request.getPayload();

        LOG.info("=== ConsoleAdapter ===");
        LOG.info("Payload-Typ: {}", request.getType());
        LOG.info("Payload-Inhalt:\n{}", new String(payload, StandardCharsets.UTF_8));
        LOG.info("============================");
    }
}
