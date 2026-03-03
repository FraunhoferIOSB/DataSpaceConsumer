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
package de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter;

/** Describes the format/structure of the payload data. */
public enum ConverterPayloadType {

    /** Structured JSON data */
    JSON("application/json"),

    /** XML document */
    XML("application/xml"),

    /** Plain text content */
    TEXT("text/plain"),

    /** Binary data (files, images, archives, etc.) */
    BINARY("application/octet-stream"),

    /** RDF data (Turtle, JSON-LD, RDF/XML, etc.) */
    RDF("application/rdf+xml"),

    /** HTML content */
    HTML("text/html"),

    /** CSV/Tabular data */
    CSV("text/csv"),

    /** Protobuf or other binary serialization */
    PROTOBUF("application/protobuf"),

    /** AASX package (Asset Administration Shell) */
    AASX("application/x-zip"),

    /** Multipart/mixed content */
    MULTIPART("multipart/mixed"),

    /** Empty/No payload */
    EMPTY(""),

    /** Unknown or unspecified format */
    UNKNOWN("application/octet-stream");

    private final String mediaType;

    ConverterPayloadType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the media type (MIME type) associated with this payload type.
     *
     * @return the media type string (e.g., "application/json", "text/plain")
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Checks if the payload is text-based and can be safely logged/displayed.
     *
     * @return true when the payload type is readable text (JSON, XML, text, RDF, HTML, CSV)
     */
    public boolean isTextBased() {
        return this == JSON
                || this == XML
                || this == TEXT
                || this == RDF
                || this == HTML
                || this == CSV;
    }

    /**
     * Checks if the payload is structured data (parseable).
     *
     * @return true when the payload type is structured (JSON, XML, RDF, CSV, Protobuf)
     */
    public boolean isStructured() {
        return this == JSON || this == XML || this == RDF || this == CSV || this == PROTOBUF;
    }

    /**
     * Checks if the payload is binary and should not be interpreted as text.
     *
     * @return true when the payload type is binary (BINARY, Protobuf, AASX, MULTIPART)
     */
    public boolean isBinary() {
        return this == BINARY || this == PROTOBUF || this == AASX || this == MULTIPART;
    }
}
