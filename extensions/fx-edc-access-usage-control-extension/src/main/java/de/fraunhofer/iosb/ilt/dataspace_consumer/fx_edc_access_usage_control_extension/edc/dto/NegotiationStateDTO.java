package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the negotiation state as returned by the EDC.
 *
 * <p>This record maps the EDC's JSON-LD response for negotiation state queries. It contains the
 * JSON-LD type, the current negotiation state (e.g. "REQUESTED", "FINALIZED", ...), and the
 * associated JSON-LD context.
 *
 * @param type JSON-LD type of the object (e.g. "@type")
 * @param state Current state of the negotiation (e.g. "REQUESTED", "FINALIZED")
 * @param context JSON-LD context with namespace aliases (see {@link ContextDTO})
 */
public record NegotiationStateDTO(
        @JsonProperty("@type") String type,
        String state,
        @JsonProperty("@context") ContextDTO context) {}
