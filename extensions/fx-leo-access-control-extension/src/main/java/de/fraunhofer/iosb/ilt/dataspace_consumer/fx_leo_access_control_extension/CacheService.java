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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.tokens.FXToken;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_leo_access_control_extension.tokens.SourceToken;

/**
 * Simple in-memory cache for source tokens and derived FX tokens.
 *
 * <p>This service uses Caffeine caches to store two mappings:
 *
 * <ul>
 *   <li>clientId (String) -> SourceToken
 *   <li>SourceToken -> FXToken
 * </ul>
 *
 * <p>Both caches apply an expiry policy based on the token's {@code expiresIn()} value. If {@code
 * expiresIn()} is provided, the stored entry will expire {@code expiresIn - 10} seconds after
 * creation to leave a small safety margin for subsequent operations. If {@code expiresIn()} is null
 * or cannot be parsed, the entry is expired immediately.
 *
 * <p>All tokens are stored as provided; this class does not validate token contents. The caches
 * provided by Caffeine are thread-safe.
 */
public class CacheService {

    private Cache<String, SourceToken> sourceTokenCache =
            Caffeine.newBuilder()
                    .expireAfter(
                            new Expiry<String, SourceToken>() {
                                @Override
                                public long expireAfterCreate(
                                        String key, SourceToken value, long currentTime) {
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
                                        String key,
                                        SourceToken value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }

                                @Override
                                public long expireAfterRead(
                                        String key,
                                        SourceToken value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }
                            })
                    .build();

    private Cache<SourceToken, FXToken> fxTokenCache =
            Caffeine.newBuilder()
                    .expireAfter(
                            new Expiry<SourceToken, FXToken>() {
                                @Override
                                public long expireAfterCreate(
                                        SourceToken key, FXToken value, long currentTime) {
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
                                        SourceToken key,
                                        FXToken value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }

                                @Override
                                public long expireAfterRead(
                                        SourceToken key,
                                        FXToken value,
                                        long currentTime,
                                        long currentDuration) {
                                    return currentDuration;
                                }
                            })
                    .build();

    /**
     * Retrieve a cached FXToken for the given SourceToken.
     *
     * @param sourceToken the source token used as cache key
     * @return the cached FXToken, or {@code null} if none is present
     */
    public FXToken getFXToken(SourceToken sourceToken) {
        return fxTokenCache.getIfPresent(sourceToken);
    }

    /**
     * Put an FXToken into the cache associated with the provided SourceToken.
     *
     * @param sourceToken the source token used as cache key
     * @param fxToken the FX token to store
     */
    public void putFXToken(SourceToken sourceToken, FXToken fxToken) {
        fxTokenCache.put(sourceToken, fxToken);
    }

    /**
     * Retrieve a cached SourceToken for the given client identifier.
     *
     * @param clientId the client identifier used as cache key
     * @return the cached SourceToken, or {@code null} if none is present
     */
    public SourceToken getSourceToken(String clientId) {
        return sourceTokenCache.getIfPresent(clientId);
    }

    /**
     * Put a SourceToken into the cache associated with the given client identifier.
     *
     * @param clientId the client identifier used as cache key
     * @param sourceToken the source token to store
     */
    public void putSourceToken(String clientId, SourceToken sourceToken) {
        sourceTokenCache.put(clientId, sourceToken);
    }
}
