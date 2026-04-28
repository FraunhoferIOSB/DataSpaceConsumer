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
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_leo_discovery_extension;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class AasDiscoveryParser {

    private AasDiscoveryParser() {}

    private static void extractFromEndpoints(
            JsonNode endpoints, String domain, List<ResultItem> result) {

        if (!endpoints.isArray()) {
            return;
        }

        for (JsonNode endpoint : endpoints) {

            String interfaceType = endpoint.path("interface").asText(null);
            JsonNode proto = endpoint.path("protocolInformation");

            String href = proto.path("href").asText(null);

            if (href == null) {
                continue;
            }

            ResultItem item = new ResultItem(href, interfaceType);

            result.add(item);
        }
    }

    public static List<ResultItem> getResults(JsonNode discoveredInfos) {
        List<ResultItem> result = new ArrayList<>();

        JsonNode assets = discoveredInfos.path("data");
        if (!assets.isArray()) {
            return result;
        }

        for (JsonNode asset : assets) {

            String domain = asset.path("domain").asText(null);

            // only top-level endpoints exist in the new structure
            extractFromEndpoints(asset.path("endpoints"), domain, result);
        }

        return result;
    }
}
