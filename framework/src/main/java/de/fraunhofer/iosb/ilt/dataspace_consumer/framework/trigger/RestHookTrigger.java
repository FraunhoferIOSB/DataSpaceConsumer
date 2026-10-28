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

import static de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType.JSON;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.AdapterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCExecutor;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCService;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.TriggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
public class RestHookTrigger extends Trigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestHookTrigger.class);

    private final DSCService mxPortService;

    public RestHookTrigger(DSCExecutor mxPortExecutor, DSCService mxPortService) {
        super(mxPortExecutor);
        this.mxPortService = mxPortService;
    }

    /**
     * HTTP POST endpoint used to trigger execution of a named MX-Port.
     *
     * <p>The endpoint returns 404 when the requested MX-Port is unknown and 403 when the rest-hook
     * trigger is not enabled for the MX-Port. On success it triggers execution and returns HTTP 204
     * (No Content).
     *
     * @param mxPortName the name of the MX-Port to execute
     * @return HTTP response indicating the result of the trigger request
     */
    @PostMapping("/trigger")
    public ResponseEntity<Void> trigger(@RequestParam("mxPortName") String mxPortName) {
        // Validate mxPortName parameter
        if (mxPortName == null || mxPortName.trim().isEmpty()) {
            LOGGER.warn("RestHook invoked with missing or empty 'mxPortName' parameter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        final String processedPortName = mxPortName.trim();

        // Check if provided name corresponds to a known MX-Port
        DSCConfig portConfig = mxPortService.getPortByName(processedPortName);
        if (portConfig == null) {
            LOGGER.warn("RestHook invoked for unknown MX-Port: {}", processedPortName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if RestHook is enabled for this port
        TriggerConfig triggerConfig = portConfig.trigger();
        if (triggerConfig == null
                || triggerConfig.getRestHook() == null
                || !Boolean.TRUE.equals(triggerConfig.getRestHook().getEnabled())) {
            LOGGER.info(
                    "RestHook is not enabled for MX-Port '{}'. Ignoring trigger.",
                    processedPortName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Start executing the MX-Port asynchronously using CompletableFuture
        executeAsync(portConfig, portConfig.timeout());
        // Return HTTP 204 (No Content) immediately after starting the execution
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trigger/config")
    public ResponseEntity<Object> trigger(@RequestBody DSCConfig mxPortConfig) {
        if (mxPortConfig.synchronous()) {
            AdapterResponse result = execute(mxPortConfig);
            if (result == null) {
                return ResponseEntity.ok().build();
            }

            Object resultBody =
                    result.payloadType() == JSON
                            ? new ObjectMapper().readTree(result.payload())
                            : result.payload();

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .headers(
                            HttpHeaders.readOnlyHttpHeaders(
                                    MultiValueMap.fromMultiValue(result.headers())))
                    .body(resultBody);
        }

        executeAsync(mxPortConfig, mxPortConfig.timeout());
        return ResponseEntity.noContent().build();
    }
}
