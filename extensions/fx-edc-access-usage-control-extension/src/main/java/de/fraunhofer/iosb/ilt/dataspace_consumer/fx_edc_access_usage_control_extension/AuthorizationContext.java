package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

/**
 * Context object representing the state of an ongoing authorization/negotiation.
 *
 * <p>This record holds the negotiation identifier returned by the negotiation initiation endpoint
 * and the associated asset identifier. It is used by the AccessUsageControl implementation to poll
 * negotiation status and to request access tokens for the negotiated asset.
 *
 * @param negotiationId the identifier of the negotiation/contract process
 * @param assetId the identifier of the asset being negotiated
 */
public record AuthorizationContext(String negotiationId, String assetId) {}
