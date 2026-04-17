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

    public EdrDTO getToken(AuthorizationContext context) {
        return tokenCache.getIfPresent(context);
    }

    public void putToken(AuthorizationContext context, EdrDTO token) {
        tokenCache.put(context, token);
    }

    public AuthorizationContext getNegotiationContext(DSPRequest request) {
        return negotiationCache.getIfPresent(request);
    }

    public void putNegotiationContext(
            DSPRequest request, AuthorizationContext authorizationContext) {
        negotiationCache.put(request, authorizationContext);
    }
}
