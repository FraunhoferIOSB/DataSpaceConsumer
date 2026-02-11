package de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;

/**
 * Request information passed to a {@link Gate} implementation.
 *
 * <p>Contains the target URL and an optional access token.
 *
 * @param url the resource URL to request
 * @param token optional access token (may be null)
 */
public record GateRequest(String url, String token) {

    /**
     * Create a {@link GateRequest} from an {@link AccessResponse}.
     *
     * @param accessResponse access response containing URL and token
     */
    public GateRequest(AccessResponse accessResponse) {
        this(accessResponse.url(), accessResponse.token());
    }
}
