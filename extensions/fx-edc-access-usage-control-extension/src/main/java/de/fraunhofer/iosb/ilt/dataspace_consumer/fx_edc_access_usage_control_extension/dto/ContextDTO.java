package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the JSON-LD context returned in responses from the EDC (Eclipse DataSpace Connector).
 *
 * <p>The context contains aliases and namespace mappings that are used within EDC JSON-LD messages.
 *
 * @param fxPolicy Alias/URI for a policy definition (JSON-LD "fx-policy")
 * @param tx Transport/transfer-specific context information (alias "tx")
 * @param txAuth Transport authentication namespace information (alias "tx-auth")
 * @param cxPolicy Context for contracts/policies (alias "cx-policy")
 * @param vocab Default vocabulary/namespace for the JSON-LD responses ("@vocab")
 * @param edc Alias/URI for EDC-specific terms in the context
 * @param odrl Alias/URI for ODRL terms in the context (e.g. policies)
 */
public record ContextDTO(
        @JsonProperty("fx-policy") String fxPolicy,
        String tx,
        @JsonProperty("tx-auth") String txAuth,
        @JsonProperty("cx-policy") String cxPolicy,
        @JsonProperty("@vocab") String vocab,
        String edc,
        String odrl) {}
