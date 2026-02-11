package de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception;

/** Exception thrown when an error occurs during the execution of an MX-Port. */
public class DSCExecuteException extends RuntimeException {
    /**
     * Constructs a new DSCExecuteException with the specified detail message.
     *
     * @param message the detail message
     */
    public DSCExecuteException(String message) {
        super(message);
    }

    /**
     * Constructs a new DSCExecuteException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DSCExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
