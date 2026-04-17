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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;

public class DiscoveryRequestFactory {

    AccessRequest getDiscoveryRequest(String baseUrl) {
        DSPFilter filter =
                new DSPFilter(
                        "'http://purl.org/dc/terms/type'.'@id'",
                        null,
                        "https://w3id.org/catenax/taxonomy#DigitalTwinRegistry");
        return new DSPRequest(filter, baseUrl + "/v3/catalog/request");
    }

    public List<AccessRequest> getGateAccessRequests(List<ResultItem> items)
            throws DSCExecuteException {

        // we only need one access request per assetId
        Set<String> includedAssetIDs = new HashSet<>();

        return items.stream()
                .filter(x -> includedAssetIDs.add(x.assetId()))
                .map(
                        x -> {
                            DSPFilter filter =
                                    new DSPFilter(
                                            "https://w3id.org/edc/v0.0.1/ns/id", null, x.assetId());
                            return (AccessRequest) new DSPRequest(filter, x.endpoint());
                        })
                .toList();
    }
}
