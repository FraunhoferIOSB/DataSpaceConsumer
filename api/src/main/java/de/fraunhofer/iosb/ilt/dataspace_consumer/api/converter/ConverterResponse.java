package de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter;

import java.util.Optional;

/**
 * Response from a Converter containing the transformed payload and metadata describing the data
 * format.
 */
public final class ConverterResponse {

    private final ConverterPayloadType type;
    private final byte[] payload;
    private final Optional<String> encoding; // e.g. "UTF-8", "ISO-8859-1"
    private final Optional<String> schema; // Optional: Schema-URL or Identifier

    /**
     * Create a full converter response with optional encoding and schema information.
     *
     * @param type the payload type
     * @param payload the payload bytes
     * @param encoding optional character encoding (e.g., "UTF-8")
     * @param schema optional schema identifier or URL
     */
    public ConverterResponse(
            ConverterPayloadType type, byte[] payload, String encoding, String schema) {
        this.type = type;
        this.payload = payload != null ? payload.clone() : new byte[0];
        this.encoding = Optional.ofNullable(encoding);
        this.schema = Optional.ofNullable(schema);
    }

    /**
     * Simplified constructor without encoding or schema metadata.
     *
     * @param type the payload type
     * @param payload the payload bytes
     */
    public ConverterResponse(ConverterPayloadType type, byte[] payload) {
        this(type, payload, null, null);
    }

    /**
     * Return the declared payload type describing the data format.
     *
     * @return the payload type
     */
    public ConverterPayloadType getType() {
        return type;
    }

    /**
     * Return a defensive copy of the converted payload bytes.
     *
     * @return a copy of the payload bytes
     */
    public byte[] getPayload() {
        return payload.clone();
    }

    /**
     * Return the optional character encoding associated with text payloads.
     *
     * @return optional encoding name (e.g., UTF-8)
     */
    public Optional<String> getEncoding() {
        return encoding;
    }

    /**
     * Return an optional schema identifier or URL describing the payload structure.
     *
     * @return optional schema identifier or URL
     */
    public Optional<String> getSchema() {
        return schema;
    }
}
