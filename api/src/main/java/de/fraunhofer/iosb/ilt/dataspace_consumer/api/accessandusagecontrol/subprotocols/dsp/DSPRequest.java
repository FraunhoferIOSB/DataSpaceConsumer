package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;

/** DSP sub-protocol request carrying filters and the DSP endpoint URL. */
public class DSPRequest implements AccessRequest {
    private final DSPFilter filters;
    private final String dspEndpointUrl;

    /**
     * Construct a DSPRequest with the given filters and provider endpoint.
     *
     * @param filters filter specification for the data request
     * @param dspEndpointUrl the endpoint URL of the DSP provider
     */
    public DSPRequest(DSPFilter filters, String dspEndpointUrl) {
        this.filters = filters;
        this.dspEndpointUrl = dspEndpointUrl;
    }

    /**
     * Return the DSP filters used for this request.
     *
     * @return the DSP filters
     */
    public DSPFilter getFilters() {
        return filters;
    }

    /**
     * Return the DSP provider endpoint URL to contact.
     *
     * @return the DSP provider endpoint URL
     */
    public String getDspEndpointUrl() {
        return dspEndpointUrl;
    }

    @Override
    public SubProtocolType getSubProtocolType() {
        return SubProtocolType.DSP;
    }
}
