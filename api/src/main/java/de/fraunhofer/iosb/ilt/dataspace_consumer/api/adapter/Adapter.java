package de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.ExtensionPoint;

/**
 * Adapter (L1) interface — adapts normalized data for a target application.
 *
 * <p>The Adapter receives an {@link ConverterResponse} produced by the Converter (L2).
 * Implementations are responsible for integrating the normalized data into the target system
 * (database, API call, message bus, etc.).
 *
 * <p>Adapters are PF4J extensions and therefore must be packaged as plugins.
 */
public interface Adapter extends ExtensionPoint {
    /**
     * Adapt the normalized data for the application.
     *
     * @param request the AdapterRequest containing the normalized payload and optional metadata
     * @throws UnsupportedPayloadTypeException if the payload type is not supported by this Adapter
     * @throws DSCExecuteException if an error occurs during adaptation
     */
    void adapt(ConverterResponse request)
            throws UnsupportedPayloadTypeException, DSCExecuteException;
}
