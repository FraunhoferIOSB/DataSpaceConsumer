package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

/** Known response formats for Gate responses, with associated media types. */
/** Known response formats for Gate responses, with associated media types. */
public enum GateResponseFormat {
    /** JSON (application/json) */
    JSON("application/json"),

    /** XML (application/xml) */
    XML("application/xml"),

    /** RDF/XML (application/rdf+xml) */
    RDF_XML("application/rdf+xml"),

    /** RDF Turtle (text/turtle) */
    RDF_TURTLE("text/turtle"),

    /** JSON-LD (application/ld+json) */
    JSON_LD("application/ld+json"),

    /** AASX package (application/aasx) */
    AASX("application/aasx"),

    /** AASX XML representation (application/aasx+xml) */
    AASX_XML("application/aasx+xml"),

    /** AASX binary (application/asset-administration-shell-package+xml) */
    AASX_BINARY("application/asset-administration-shell-package+xml"),

    /** AASX package generic (application/asset-administration-shell-package) */
    AASX_PACKAGE("application/asset-administration-shell-package"),

    /** Binary stream (application/octet-stream) */
    OCTET_STREAM("application/octet-stream"),

    /** Multipart form data (multipart/form-data) */
    MULTIPART_FORM_DATA("multipart/form-data");

    private final String mediaType;

    GateResponseFormat(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Return the MIME/media type string associated with this format.
     *
     * @return the MIME/media type string associated with this format
     */
    public String getMediaType() {
        return mediaType;
    }
}
