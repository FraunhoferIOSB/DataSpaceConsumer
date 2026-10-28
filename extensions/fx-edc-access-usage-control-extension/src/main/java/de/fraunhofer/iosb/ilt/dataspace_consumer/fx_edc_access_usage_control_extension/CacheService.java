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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.EdrDTO;

/**
 * CacheService provides in-memory caching for negotiation contexts and EDR tokens used by the FX
 * EDC access/usage control extension.
 *
 * <p>This class uses Caffeine caches to store:
 *
 * <ul>
 *   <li>negotiationCache: maps {@link DSPRequest} -> {@link AuthorizationContext} and expires
 *       entries 24 hours after write.
 *   <li>tokenCache: maps {@link AuthorizationContext} -> {@link EdrDTO} and uses the token's
 *       reported lifetime ({@link EdrDTO#expiresIn()}) to determine expiration. If the token does
 *       not provide expiration information it is considered expired immediately.
 * </ul>
 *
 * The caches are thread-safe as provided by Caffeine.
 */
class CacheService {

    private Cache<DSPRequest, AuthorizationContext> negotiationCache =
            Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build();

    Cache<AuthorizationContext, EdrDTO> tokenCache =
            Caffeine.newBuilder()
                    .expireAfter(
                            new Expiry<AuthorizationContext, EdrDTO>() {
                                @Override
                                public long expireAfterCreate(
                                        AuthorizationContext key, EdrDTO value, long currentTime) {
                                    if (value.expiresIn() != null) {
                                        long seconds =
                                                Long.parseLong(value.expiresIn())
                                                        - 10; // 10s for all subsequent operations
                                        // until
                                        // token expires
                                        return TimeUnit.SECONDS.toNanos(seconds);
                                    }
                                    return 0; // no expiration info, expire immediately
                                }

                                @Override
                                public long expireAfterUpdate(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }

                                @Override
                                public long expireAfterRead(
                                        AuthorizationContext key,
                                        EdrDTO value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }
                            })
                    .build();

    /**
     * Retrieve an EDR token for the given authorization context from the cache.
     *
     * @param context the authorization context used as the cache key
     * @return the cached {@link EdrDTO} for the provided context, or {@code null} if no token is
     *     cached or it already expired
     */
    public EdrDTO getToken(AuthorizationContext context) {
        return tokenCache.getIfPresent(context);
    }

    /**
     * Store an EDR token in the cache for the provided authorization context.
     *
     * <p>The token's expiration is derived from {@link EdrDTO#expiresIn()} and the cache will evict
     * the entry shortly before the token expires.
     *
     * @param context the authorization context to associate with the token
     * @param token the {@link EdrDTO} containing token and lifetime information
     */
    public void putToken(AuthorizationContext context, EdrDTO token) {
        tokenCache.put(context, token);
    }

    /**
     * Retrieve a previously stored negotiation context for a DSP request.
     *
     * @param request the DSP request whose negotiation context is requested
     * @return the cached {@link AuthorizationContext} associated with the request, or {@code null}
     *     if none is present
     */
    public AuthorizationContext getNegotiationContext(DSPRequest request) {
        return negotiationCache.getIfPresent(request);
    }

    /**
     * Store a negotiation context for the given DSP request.
     *
     * @param request the DSP request to associate the negotiation context with
     * @param authorizationContext the {@link AuthorizationContext} produced by the negotiation to
     *     cache
     */
    public void putNegotiationContext(
            DSPRequest request, AuthorizationContext authorizationContext) {
        negotiationCache.put(request, authorizationContext);
    }
}
