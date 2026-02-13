package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.trigger;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCExecutor;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCService;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.TriggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestHookTrigger extends Trigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestHookTrigger.class);

    private final DSCService mxPortService;

    public RestHookTrigger(DSCExecutor mxPortExecutor, DSCService mxPortService) {
        super(mxPortExecutor);
        this.mxPortService = mxPortService;
    }

    /**
     * HTTP GET endpoint used to trigger execution of a named MX-Port.
     *
     * <p>The endpoint returns 404 when the requested MX-Port is unknown and 403 when the rest-hook
     * trigger is not enabled for the MX-Port. On success it triggers execution and returns HTTP 204
     * (No Content).
     *
     * @param mxPortName the name of the MX-Port to execute
     * @return HTTP response indicating the result of the trigger request
     */
    @PostMapping("/trigger")
    public ResponseEntity<Void> trigger(@RequestBody String body) throws JsonMappingException {
        // Parse body json to portName
        String mxPortName;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            JsonNode node = null;
            if (root.has("mxPortName")) {
                node = root.get("mxPortName");
            }

            if (node == null || node.isNull() || node.asText().trim().isEmpty()) {
                LOGGER.warn(
                        "RestHook invoked with missing or empty 'mxPortName' in body: {}", body);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            mxPortName = node.asText().trim();
        } catch (JsonProcessingException e) {
            LOGGER.warn(
                    "Failed to parse RestHook request body: {} - raw body: {}",
                    e.getMessage(),
                    body);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check if provided name corresponds to a known MX-Port
        DSCConfig portConfig = mxPortService.getPortByName(mxPortName);
        if (portConfig == null) {
            LOGGER.warn("RestHook invoked for unknown MX-Port: {}", mxPortName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if RestHook is enabled for this port
        TriggerConfig triggerConfig = portConfig.getTrigger();
        if (triggerConfig == null
                || triggerConfig.getRestHook() == null
                || !Boolean.TRUE.equals(triggerConfig.getRestHook().getEnabled())) {
            LOGGER.info("RestHook is not enabled for MX-Port '{}'. Ignoring trigger.", mxPortName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Start executing the MX-Port asynchronously using CompletableFuture
        CompletableFuture.runAsync(
                () -> {
                    execute(mxPortName);
                });

        // Return HTTP 204 (No Content) immediately after starting the execution
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
