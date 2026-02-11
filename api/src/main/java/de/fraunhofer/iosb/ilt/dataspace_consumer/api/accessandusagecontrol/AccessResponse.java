package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol;

/**
 * Access response returned by access control negotiations.
 *
 * @param url the URL for the granted access
 * @param token the access token or credential (may be null)
 * @param identifier optional identifier for the granted access (implementation-defined)
 */
public record AccessResponse(String url, String token, Object identifier) {}
