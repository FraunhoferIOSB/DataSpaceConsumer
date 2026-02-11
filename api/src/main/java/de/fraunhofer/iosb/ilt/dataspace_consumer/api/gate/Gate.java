package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * Gate interface for fetching data in one of the requested serialization formats.
 *
 * <p>Implementations should try to return data in one of the desired formats (in order of
 * preference). If none of the requested formats can be produced, implementations must throw {@link
 * GateFormatNotSupportedException}.
 */
public interface Gate extends ExtensionPoint {
    /**
     * Fetch the data for the given request in one of the specified formats.
     *
     * @param requestInfo request information (url, token)
     * @param desiredFormats desired data response serialization formats (preference order). If
     *     empty, the implementation may return a default format.
     * @return GateResponse containing payload and metadata in one of the desired formats
     * @throws GateFormatNotSupportedException if the Gate cannot provide any of the desired formats
     */
    GateResponse getData(GateRequest requestInfo, List<GateResponseFormat> desiredFormats)
            throws GateFormatNotSupportedException;
}
