package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol;

/** Marker interface for access request types used by access and usage control sub-protocols. */
public interface AccessRequest {
    /**
     * Return the sub-protocol type applicable for this request.
     *
     * @return the sub-protocol type applicable for this request
     */
    SubProtocolType getSubProtocolType();
}
