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
