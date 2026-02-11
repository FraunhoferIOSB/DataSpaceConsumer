package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Response returned by a {@link Gate} containing the payload and metadata.
 *
 * <p>The payload is kept as a byte[]; specific parsers (JSON, XML, RDF, AASX) can be applied by
 * implementations. Utility methods such as {@link #asString()} and {@link #getPayloadBytes()} are
 * provided for convenience.
 */
public final class GateResponse {

    private final int status;
    private final GateResponseFormat format; // may be null if unknown
    private final Map<String, List<String>> headers;
    private final byte[] payload;
    private final Optional<String> fileName; // if provided

    /**
     * Construct a GateResponse holding status, optional format, headers and payload.
     *
     * @param status HTTP-like status code (e.g., 200 for success)
     * @param format optional response format
     * @param headers HTTP headers and metadata
     * @param payload raw payload bytes
     * @param fileName optional filename suggested by provider
     */
    public GateResponse(
            int status, // HTTP-like status code (e.g., 200 for success, 404 for not found, etc.)
            GateResponseFormat format,
            Map<String, List<String>> headers,
            byte[] payload,
            String fileName) {
        this.status = status;
        this.format = format;
        this.headers =
                headers != null
                        ? Collections.unmodifiableMap(new HashMap<>(headers))
                        : Collections.emptyMap();
        this.payload = payload != null ? payload.clone() : new byte[0];
        this.fileName = Optional.ofNullable(fileName);
    }

    // Factory convenience
    /**
     * Create a response instance from raw parts.
     *
     * @param status HTTP-like status code
     * @param mediaType the media type string (unused, kept for compatibility)
     * @param format optional parsed format
     * @param headers response headers
     * @param payload raw payload bytes
     * @param fileName optional filename
     * @return a new {@link GateResponse}
     */
    public static GateResponse of(
            int status,
            String mediaType,
            GateResponseFormat format,
            Map<String, List<String>> headers,
            byte[] payload,
            String fileName) {
        return new GateResponse(status, format, headers, payload, fileName);
    }

    // Basic getters
    /**
     * Return the HTTP-like status code for the response.
     *
     * @return HTTP-like status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Return the optionally detected response format.
     *
     * @return optional detected response format
     */
    public Optional<GateResponseFormat> getFormat() {
        return Optional.ofNullable(format);
    }

    /**
     * Return response headers and metadata.
     *
     * @return response headers (never null)
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Return the optional suggested filename from the provider.
     *
     * @return optional filename suggested by the provider
     */
    public Optional<String> getFileName() {
        return fileName;
    }

    /**
     * Return the length of the payload in bytes.
     *
     * @return the payload length in bytes
     */
    public long getContentLength() {
        return payload.length;
    }

    /**
     * Returns an InputStream for the payload.
     *
     * @return an {@link InputStream} that reads the payload bytes
     */
    public InputStream getPayloadStream() {
        return new ByteArrayInputStream(payload);
    }

    /**
     * Returns the payload as a byte[] (copy).
     *
     * @return a copy of the payload bytes
     */
    public byte[] getPayloadBytes() {
        return payload.clone();
    }

    /**
     * Returns the payload as a String using UTF-8. For binary data the result is not meaningful.
     *
     * @return the payload interpreted as UTF-8 text
     */
    public String asString() {
        return asString(StandardCharsets.UTF_8);
    }

    /**
     * Returns the payload as a String using the specified Charset.
     *
     * @param charset the charset to use for decoding
     * @return the payload interpreted using the given charset
     */
    public String asString(Charset charset) {
        return new String(payload, charset);
    }

    /**
     * Writes the payload to the given file.
     *
     * @param target the target file to write to
     * @throws IOException if writing fails
     */
    public void writeTo(Path target) throws IOException {
        Files.createDirectories(
                target.getParent() != null
                        ? target.getParent()
                        : target.toAbsolutePath().getParent());
        Files.write(target, payload);
    }
}
