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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.trigger;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.UnsupportedPayloadTypeException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateFormatNotSupportedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Central exception handler mapping exceptions raised during MX-Port execution and request handling
 * to HTTP status codes with descriptive bodies.
 *
 * <p>Exception messages and root causes are intentionally included in the response body. Only
 * configure this handler in environments where exposing internal error detail to the requester is
 * acceptable.
 */
@RestControllerAdvice
public class DSCExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSCExceptionHandler.class);

    /**
     * Configuration or argument validation failure, typically thrown by a plugin's
     * {@code setConfiguration} implementation.
     *
     * @param ex the illegal argument
     * @param request the current request
     * @return a {@code 400 Bad Request} response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /**
     * Missing or malformed request parameter, e.g. a missing {@code mxPortName} on the {@code
     * /trigger} endpoint.
     *
     * @param ex the binding exception
     * @param request the current request
     * @return a {@code 400 Bad Request} response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /**
     * Malformed or unreadable request body, e.g. invalid JSON on {@code /trigger/config}.
     *
     * @param ex the message-not-readable exception
     * @param request the current request
     * @return a {@code 400 Bad Request} response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /**
     * Unknown path or method, raised by Spring when no handler matches.
     *
     * @param ex the no-resource-found exception
     * @param request the current request
     * @return a {@code 404 Not Found} response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    /**
     * The configured pipeline cannot process the payload type produced by the converter, or the
     * gate cannot provide data in any requested format. The request is well-formed but semantically
     * incompatible with the configured pipeline.
     *
     * @param ex the unsupported-type or unsupported-format exception
     * @param request the current request
     * @return a {@code 422 Unprocessable Content} response
     */
    @ExceptionHandler({UnsupportedPayloadTypeException.class, GateFormatNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleUnsupported(
            RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex, request);
    }

    /**
     * A requested operation or format is not yet implemented (e.g. RDF conversion in the simple
     * converter).
     *
     * @param ex the unsupported-operation exception
     * @param request the current request
     * @return a {@code 501 Not Implemented} response
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(
            UnsupportedOperationException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_IMPLEMENTED, ex, request);
    }

    /**
     * I/O failure while contacting an upstream service, typically raised by discovery or gate
     * implementations during outbound HTTP calls.
     *
     * @param ex the I/O exception
     * @param request the current request
     * @return a {@code 502 Bad Gateway} response
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(
            IOException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex, request);
    }

    /**
     * Execution interrupted (thread interrupted), typically indicating the service is shutting down
     * or the request was cancelled. The interrupt status is restored before returning.
     *
     * @param ex the interrupted exception
     * @param request the current request
     * @return a {@code 503 Service Unavailable} response
     */
    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorResponse> handleInterrupted(
            InterruptedException ex, HttpServletRequest request) {
        Thread.currentThread().interrupt();
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex, request);
    }

    /**
     * Execution failure raised during the MX-Port workflow. The status is refined by the underlying
     * cause: a {@link TimeoutException} maps to {@code 504 Gateway Timeout}, an {@link
     * InterruptedException} maps to {@code 503 Service Unavailable}, any other cause maps to {@code
     * 500 Internal Server Error}.
     *
     * @param ex the execution exception
     * @param request the current request
     * @return a status-appropriate response
     */
    @ExceptionHandler(DSCExecuteException.class)
    public ResponseEntity<ErrorResponse> handleExecuteException(
            DSCExecuteException ex, HttpServletRequest request) {
        Throwable cause = unwrap(ex);
        if (cause instanceof TimeoutException) {
            return build(HttpStatus.GATEWAY_TIMEOUT, ex, request);
        }
        if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return build(HttpStatus.SERVICE_UNAVAILABLE, ex, request);
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    /**
     * Fallback for any otherwise unhandled exception.
     *
     * @param ex the unexpected exception
     * @param request the current request
     * @return a {@code 500 Internal Server Error} response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, Exception ex, HttpServletRequest request) {
        log(status, ex);
        ErrorResponse body =
                new ErrorResponse(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        rootCauseMessage(ex),
                        request.getMethod(),
                        request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private void log(HttpStatus status, Exception ex) {
        if (status.is5xxServerError()) {
            LOGGER.error("Handling {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        } else {
            LOGGER.warn("Handling {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private static Throwable unwrap(Throwable t) {
        Throwable cause = t.getCause();
        return cause != null ? cause : t;
    }

    private static String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur == t || cur.getMessage() == null ? null : cur.getMessage();
    }

    /**
     * Error response body describing the failure.
     *
     * @param timestamp ISO-8601 instant the error was produced
     * @param status numeric HTTP status code
     * @param error HTTP reason phrase for the status
     * @param exception simple name of the thrown exception class
     * @param message exception detail message
     * @param rootCause message of the root cause, or {@code null} if none
     * @param method HTTP method of the failed request
     * @param path request URI of the failed request
     */
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String exception,
            String message,
            String rootCause,
            String method,
            String path) {}
}
