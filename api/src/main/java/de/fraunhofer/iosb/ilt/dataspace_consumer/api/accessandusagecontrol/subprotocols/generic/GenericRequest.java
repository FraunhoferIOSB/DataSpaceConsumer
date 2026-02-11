package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.generic;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;

/**
 * Generic access request carrying a JSON payload for the GENERIC sub-protocol.
 *
 * @param payload JSON payload to be used by the access control implementation
 */
public record GenericRequest(JsonNode payload) implements AccessRequest {
    @Override
    public SubProtocolType getSubProtocolType() {
        return SubProtocolType.GENERIC;
    }
}
