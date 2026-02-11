package de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;

/** Thrown when an adapter cannot process a payload due to unsupported type/format. */
public class UnsupportedPayloadTypeException extends RuntimeException {

    /** The unsupported payload type. */
    private final ConverterPayloadType unsupportedType;

    /** Name of the adapter that raised the exception. */
    private final String adapterName;

    /**
     * Create an exception indicating the adapter cannot handle a payload type.
     *
     * @param type the unsupported payload type
     * @param adapterName the name of the adapter that raised the exception
     */
    public UnsupportedPayloadTypeException(ConverterPayloadType type, String adapterName) {
        super(String.format("Adapter '%s' does not support payload type '%s'", adapterName, type));
        this.unsupportedType = type;
        this.adapterName = adapterName;
    }

    /**
     * Create an exception with additional details explaining why the type is unsupported.
     *
     * @param type the unsupported payload type
     * @param adapterName the name of the adapter
     * @param details additional details explaining why the type is unsupported
     */
    public UnsupportedPayloadTypeException(
            ConverterPayloadType type, String adapterName, String details) {
        super(
                String.format(
                        "Adapter '%s' does not support payload type '%s': %s",
                        adapterName, type, details));
        this.unsupportedType = type;
        this.adapterName = adapterName;
    }

    /**
     * Return the unsupported payload type that caused this exception.
     *
     * @return the unsupported payload type
     */
    public ConverterPayloadType getUnsupportedType() {
        return unsupportedType;
    }

    /**
     * Return the name of the adapter that raised this exception.
     *
     * @return the adapter name that raised the exception
     */
    public String getAdapterName() {
        return adapterName;
    }
}
