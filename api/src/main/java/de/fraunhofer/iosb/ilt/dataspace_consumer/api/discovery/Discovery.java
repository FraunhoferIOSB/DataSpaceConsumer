package de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery;

import java.util.List;
import java.util.stream.Collectors;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import org.pf4j.ExtensionPoint;

/**
 * Interface for discovery implementations.
 *
 * @param <C> Type of the discovered information returned (e.g. a DTO or a container object holding
 *     metadata)
 */
public interface Discovery<C> extends ExtensionPoint {
    /**
     * Returns the {@link AccessRequest} template that must be used to perform a discovery request.
     *
     * <p>Implementations should provide the minimal information required so that the caller can
     * check discovery authorization or trigger the discovery (for example tenant-specific or
     * context-related details).
     *
     * @return an {@link AccessRequest} instance containing the information needed to perform
     *     discovery requests
     */
    AccessRequest getDiscoveryAccessRequest();

    /**
     * Executes the discovery using the provided access response and returns the discovered
     * information.
     *
     * <p>The method receives the previously obtained {@link AccessResponse} and should extract or
     * gather all relevant information required for subsequent processing steps.
     *
     * @param request the {@link AccessResponse} containing information that can assist with
     *     discovery (e.g. authorization or context hints)
     * @return the discovered information of type {@code C}
     */
    C discover(AccessResponse request);

    /**
     * Derives a list of {@link AccessRequest}s from the discovered information that are required to
     * access individual gate resources.
     *
     * <p>Typical implementations produce one {@link AccessRequest} per discovered resource (e.g.
     * for different ports, endpoints or sub-resources).
     *
     * @param discoveredInfos the information produced by {@link #discover(AccessResponse)}
     * @return a list of {@link AccessRequest} objects that can be used for gate calls
     */
    List<AccessRequest> getGateAccessRequests(C discoveredInfos);

    /**
     * Helper method that converts a list of {@link AccessResponse} objects into a list of {@link
     * GateRequest} objects.
     *
     * <p>The default implementation creates a new {@link GateRequest} for each {@link
     * AccessResponse}. Implementations may override this method if a custom mapping logic is
     * required.
     *
     * @param accessResponses list of access responses to be mapped to gate requests
     * @param discoverdInfos the discovered information (not required for the default conversion but
     *     useful for specialized overrides)
     * @return list of {@link GateRequest} objects
     */
    default List<GateRequest> convertToGateRequests(
            List<AccessResponse> accessResponses, C discoverdInfos) {
        return accessResponses.stream().map(GateRequest::new).collect(Collectors.toList());
    }
}
