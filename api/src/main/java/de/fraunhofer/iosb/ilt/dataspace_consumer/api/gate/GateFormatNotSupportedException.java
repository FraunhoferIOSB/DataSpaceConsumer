package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

/** Thrown when a Gate implementation cannot provide data in any of the requested formats. */
public class GateFormatNotSupportedException extends RuntimeException {
    /**
     * Create an exception describing unsupported response formats.
     *
     * @param message human-readable message describing the unsupported formats
     */
    public GateFormatNotSupportedException(String message) {
        super(message);
    }

    /**
     * Create an exception with nested cause information for debugging.
     *
     * @param message human-readable message describing the error
     * @param cause the underlying cause for the failure
     */
    public GateFormatNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
