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

    public FXToken getFXToken(SourceToken sourceToken) {
        return fxTokenCache.getIfPresent(sourceToken);
    }

    public void putFXToken(SourceToken sourceToken, FXToken fxToken) {
        fxTokenCache.put(sourceToken, fxToken);
    }

    public SourceToken getSourceToken(String clientId) {
        return sourceTokenCache.getIfPresent(clientId);
    }

    public void putSourceToken(String clientId, SourceToken sourceToken) {
        sourceTokenCache.put(clientId, sourceToken);
    }
}
