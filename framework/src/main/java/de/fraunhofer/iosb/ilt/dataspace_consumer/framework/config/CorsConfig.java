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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

import java.util.List;

import static java.util.Optional.ofNullable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS configuration to allow requests from multiple sources. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String URL_PATH1 = "/**";

    @Value("${cors.enabled:false}")
    private boolean corsEnabled;

    @Value("${cors.allowCredentials:false}")
    private boolean corsAllowCredentials;

    @Value("${cors.allowedOrigins:}")
    private List<String> corsAllowedOrigins;

    @Value("${cors.allowedMethods:}")
    private List<String> corsAllowedMethods;

    @Value("${cors.allowedHeaders:}")
    private List<String> corsAllowedHeaders;

    @Value("${cors.exposedHeaders:}")
    private List<String> corsExposedHeaders;

    @Value("${cors.maxAge:1800}")
    private long corsMaxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsEnabled) {
            return;
        }
        CorsRegistration registration = registry.addMapping(URL_PATH1);

        List<String> origins =
                ofNullable(corsAllowedOrigins)
                        .map(x -> x.stream().map(String::trim).filter(s -> !s.isEmpty()).toList())
                        .orElse(List.of());

        boolean containsWildcard = origins.contains("*");
        if (corsAllowCredentials) {
            // With credentials, never use allowedOrigins("*"). Use patterns or explicit origins.
            if (containsWildcard) {
                registration.allowedOriginPatterns("*");
            } else if (!origins.isEmpty()) {
                registration.allowedOriginPatterns(origins.toArray(String[]::new));
            }
            // else: no origin configured -> none allowed (safe default)
        } else {
            // No credentials: "*" is allowed with allowedOrigins
            if (origins.isEmpty() || containsWildcard) {
                registration.allowedOrigins("*");
            } else {
                registration.allowedOrigins(origins.toArray(String[]::new));
            }
        }
        registration.allowedMethods(corsAllowedMethods.toArray(String[]::new));
        registration.allowedHeaders(corsAllowedHeaders.toArray(String[]::new));
        registration.exposedHeaders(corsExposedHeaders.toArray(String[]::new));
        registration.allowCredentials(corsAllowCredentials);
        registration.maxAge(corsMaxAge);
    }
}
