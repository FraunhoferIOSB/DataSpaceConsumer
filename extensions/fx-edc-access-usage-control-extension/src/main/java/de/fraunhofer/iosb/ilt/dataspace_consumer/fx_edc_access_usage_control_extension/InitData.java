package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

/**
 * Container holding an extracted policy JSON string and the associated asset id.
 *
 * <p>This record is used internally to return the raw policy JSON (as a string) along with the
 * asset identifier that the policy targets. The policy string is suitable for inclusion in
 * subsequent negotiation requests sent to the EDC endpoints.
 *
 * @param policy the raw JSON policy string
 * @param assetId the identifier of the asset the policy applies to
 */
public record InitData(String policy, String assetId) {}
