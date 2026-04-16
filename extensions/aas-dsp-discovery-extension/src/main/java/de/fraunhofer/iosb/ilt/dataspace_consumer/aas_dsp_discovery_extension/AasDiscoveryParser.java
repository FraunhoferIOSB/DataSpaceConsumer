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
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_discovery_extension;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

public class AasDiscoveryParser {

    private AasDiscoveryParser() {}

    private static final Pattern SUBPROTOCOL_PATTERN =
            Pattern.compile("d=([^;]+);[^;=\s]+ndpoint=([^;\"]+)");

    private static void extractFromEndpoints(JsonNode endpoints, List<ResultItem> result) {

        if (!endpoints.isArray()) {
            return;
        }

        for (JsonNode endpoint : endpoints) {

            String interfaceType = endpoint.path("interface").asText(null);
            JsonNode proto = endpoint.path("protocolInformation");

            String href = proto.path("href").asText(null);
            String subBody = proto.path("subprotocolBody").asText(null);

            if (href == null || subBody == null) {
                continue;
            }

            Matcher matcher = SUBPROTOCOL_PATTERN.matcher(subBody);
            if (!matcher.find()) {
                continue;
            }

            String assetId = matcher.group(1);
            String dspEndpoint = matcher.group(2);

            ResultItem item = new ResultItem(assetId, dspEndpoint, href, interfaceType);

            result.add(item);
        }
    }

    public static List<ResultItem> getResults(JsonNode discoveredInfos) {
        List<ResultItem> result = new ArrayList<>();
        JsonNode assets = discoveredInfos.path("result");
        if (!assets.isArray()) {
            return result;
        }

        for (JsonNode asset : assets) {

            // asset-level endpoints
            extractFromEndpoints(asset.path("endpoints"), result);

            // submodel endpoints
            JsonNode submodels = asset.path("submodelDescriptors");
            if (submodels.isArray()) {
                for (JsonNode submodel : submodels) {
                    extractFromEndpoints(submodel.path("endpoints"), result);
                }
            }
        }

        return result;
    }
}
