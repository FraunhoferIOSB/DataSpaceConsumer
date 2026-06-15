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

public class LoggingUtil {

    private LoggingUtil() {}

    /**
     * Masks the central part of a token string leaving the specified number of characters visible
     * at both ends.
     *
     * @param token the token string to mask
     * @param visible number of characters to keep visible at each end
     * @return the masked token string
     */
    public static String maskToken(String token, int visible) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        if (token.length() <= visible * 2) {
            return "*".repeat(token.length());
        }

        String start = token.substring(0, visible);
        String end = token.substring(token.length() - visible);
        String masked = "*".repeat(token.length() - (visible * 2));

        return start + masked + end;
    }
}
