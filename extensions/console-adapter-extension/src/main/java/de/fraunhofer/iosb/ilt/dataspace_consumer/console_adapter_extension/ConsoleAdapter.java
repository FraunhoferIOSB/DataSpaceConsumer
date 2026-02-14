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
