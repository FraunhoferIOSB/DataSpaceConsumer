package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;

/**
 * DSP Access and Usage Control Interface
 *
 * @param <C> Context type for negotiation process
 */
public interface DSPAccessAndUsageControl<C> extends AccessAndUsageControl {
    /**
     * Initializes the negotiation process
     *
     * @param accessRequest Optional input data for initializing the negotiation process if empty
     *     retrieve from configuration or use default initialization
     * @return C context object which identifies the negotiation process
     * @throws DSCExecuteException in case of errors during initialization
     */
    C initAccess(DSPRequest accessRequest) throws DSCExecuteException;

    /**
     * Checks if negotiation process is finalized
     *
     * @param context C context object which identifies the negotiation process
     * @return true if finalized, false otherwise
     * @throws DSCExecuteException in case of errors during checking
     */
    boolean isNegotiationFinalized(C context) throws DSCExecuteException;

    /**
     * Get token object for gate to access data
     *
     * @param context C context object which identifies the negotiation process
     * @return T token object for accessing data (e.g. include endpoint url and token)
     * @throws DSCExecuteException in case of errors during token retrieval
     */
    AccessResponse getTokenForAccess(C context) throws DSCExecuteException;

    @Override
    default List<SubProtocolType> getSupportedSubProtocolTypes() {
        return List.of(SubProtocolType.DSP);
    }

    @Override
    default AccessResponse retrieveAccessInformation(AccessRequest accessRequest)
            throws DSCExecuteException {
        if (accessRequest instanceof DSPRequest dSPRequest) {
            DSPExcecuter<C> excecuter = new DSPExcecuter<>(this);
            return excecuter.retrieveAccessInfos(dSPRequest);
        }
        throw new DSCExecuteException("Invalid Access Request Type for DSP Access Control");
    }
}
