package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.ExtensionPoint;

/**
 * Extension point for access and usage control implementations.
 *
 * <p>Provides methods to negotiate and retrieve access tokens or credentials for requested data.
 */
public interface AccessAndUsageControl extends ExtensionPoint {
    /**
     * Gets the list of supported sub-protocol types.
     *
     * @return A list of supported SubProtocolType.
     */
    List<SubProtocolType> getSupportedSubProtocolTypes();

    /**
     * Retrieves access information based on the provided access request.
     *
     * @param accessRequest The request containing details about the access being requested.
     * @return The response containing the access information.
     * @throws DSCExecuteException If an error occurs during the retrieval process.
     */
    AccessResponse retrieveAccessInformation(AccessRequest accessRequest)
            throws DSCExecuteException;
}
