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

/**
 * Utility parser for Asset Administration Shell (AAS) DSP discovery JSON payloads.
 *
 * <p>Provides a simple static API to extract discovery result items from the JSON structure
 * returned by an AAS DSP registry. The class is stateless and not instantiable.
 *
 * <p>The parser looks for a top-level "result" array of asset objects. For each asset it inspects
 * the "endpoints" array and the "submodelDescriptors[*].endpoints" arrays. From each endpoint it
 * reads "protocolInformation.subprotocolBody" and extracts the DSP asset identifier and DSP
 * endpoint using the expected subprotocol encoding (pattern:
 * "d=<assetId>;...ndpoint=<dspEndpoint>").
 *
 * <p>Only a subset of fields are returned in {@link ResultItem}: the extracted asset id, the DSP
 * endpoint URL, the registry href and the declared interface type. If the input does not match the
 * expected structure an empty list is returned.
 */
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

            if (href != null && subBody != null) {
                Matcher matcher = SUBPROTOCOL_PATTERN.matcher(subBody);
                if (matcher.find()) {
                    String assetId = matcher.group(1);
                    String dspEndpoint = matcher.group(2);

                    ResultItem item = new ResultItem(assetId, dspEndpoint, href, interfaceType);
                    result.add(item);
                }
            }
        }
    }

    /**
     * Parse an AAS DSP discovery JSON payload and extract discovery result items.
     *
     * <p>This method scans the provided discovery JSON for asset entries and their endpoints,
     * including endpoints listed on submodel descriptors. For each endpoint it attempts to extract
     * the following values from the protocol information's subprotocol body using a pattern match:
     *
     * <ul>
     *   <li>assetId - the DSP asset identifier
     *   <li>dspEndpoint - the DSP endpoint URL
     *   <li>href - the resource href returned by the registry
     *   <li>interfaceType - the interface type declared on the endpoint
     * </ul>
     *
     * If the expected structure is not present or no endpoints match the pattern an empty list is
     * returned.
     *
     * @param discoveredInfos the parsed discovery payload (expected to contain a "result" array)
     * @return list of discovered {@code ResultItem} objects; empty list when no results found
     */
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
