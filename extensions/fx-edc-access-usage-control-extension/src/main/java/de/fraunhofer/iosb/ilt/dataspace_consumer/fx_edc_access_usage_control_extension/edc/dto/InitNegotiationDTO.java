package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the EDC response when initializing a contract negotiation.
 *
 * <p>This record contains the essential metadata returned by the EDC when creating (initiating) a
 * negotiation: type information in JSON-LD format, the generated negotiation/resource ID, the
 * creation timestamp, and the associated JSON-LD context.
 *
 * @param type the JSON-LD type of the returned object (e.g., "@type")
 * @param id the ID assigned by the EDC to the started negotiation (e.g., "@id")
 * @param createdAt creation time as a Unix timestamp in milliseconds
 * @param context JSON-LD context with namespace aliases (see {@link ContextDTO})
 */
public record InitNegotiationDTO(
        @JsonProperty("@type") String type,
        @JsonProperty("@id") String id,
        Long createdAt,
        @JsonProperty("@context") ContextDTO context) {}
