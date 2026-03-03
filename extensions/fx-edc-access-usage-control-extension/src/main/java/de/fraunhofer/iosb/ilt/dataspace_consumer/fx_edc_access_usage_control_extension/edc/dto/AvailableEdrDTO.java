package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO (Data Transfer Object) for an available EDR entry (Endpoint Data Reference) as returned by
 * the EDC (Eclipse DataSpace Connector).
 *
 * <p>This record represents the metadata of an EDR entry which is used to subsequently retrieve the
 * corresponding DataAddress / token information.
 *
 * @param id The unique EDR identifier (@id) of the entry
 * @param type The RDF/JSON-LD type of the object (@type)
 * @param providerId The provider ID that offers the EDR
 * @param assetId The asset ID that the EDR refers to
 * @param agreementId The associated contract ID (if any)
 * @param transferProcessId The associated transfer process ID (if any)
 * @param createdAt Creation timestamp of the EDR entry (Unix milliseconds)
 * @param contractNegotiationId The negotiation ID to which this EDR belongs
 * @param context Additional JSON-LD context information
 */
public record AvailableEdrDTO(
        @JsonProperty("@id") String id,
        @JsonProperty("@type") String type,
        String providerId,
        String assetId,
        String agreementId,
        String transferProcessId,
        Long createdAt,
        String contractNegotiationId,
        @JsonProperty("@context") ContextDTO context) {}
