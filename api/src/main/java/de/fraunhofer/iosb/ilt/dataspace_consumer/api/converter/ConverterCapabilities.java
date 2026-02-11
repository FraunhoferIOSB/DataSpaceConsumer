package de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;

/**
 * Capabilities of a Converter plugin.
 *
 * <p>Describes which data serialization formats a Converter can handle and provides an optional
 * human-readable description.
 */
public class ConverterCapabilities {
    private final List<GateResponseFormat> supportedFormats;

    /**
     * Create converter capabilities.
     *
     * @param supportedFormats list of data formats this converter supports; must not be null or
     *     empty
     * @throws IllegalArgumentException if {@code supportedFormats} is null or empty
     */
    public ConverterCapabilities(List<GateResponseFormat> supportedFormats) {
        if (supportedFormats == null || supportedFormats.isEmpty()) {
            throw new IllegalArgumentException("At least one format must be supported");
        }
        this.supportedFormats = supportedFormats;
    }

    /**
     * Returns {@code true} when this converter supports the given format.
     *
     * @param format the data format to check (not null)
     * @return true if supported
     */
    public boolean supports(GateResponseFormat format) {
        return supportedFormats.contains(format);
    }

    /**
     * Returns an immutable view of all supported formats.
     *
     * @return list of supported formats
     */
    public List<GateResponseFormat> getSupportedFormats() {
        return supportedFormats;
    }
}
