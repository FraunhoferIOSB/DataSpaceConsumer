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
package de.fraunhofer.iosb.ilt.dataspace_consumer.aas_dsp_endpoints_extension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery.Discovery;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import org.pf4j.Extension;

@Extension
public class DiscoveryImpl implements Discovery<Void>, Configurable {

    private List<ResultItem> endpoints;

    @Override
    public AccessRequest getDiscoveryAccessRequest() throws DSCExecuteException {

        // no request is needed
        return null;
    }

    @Override
    public Void discover(AccessResponse accessResponse) throws DSCExecuteException {

        // accessResponse is expected to be null

        return null;
    }

    @Override
    public List<AccessRequest> getGateAccessRequests(Void empty) throws DSCExecuteException {

        // we only need one access request per assetId
        Set<String> includedAssetIDs = new HashSet<>();

        if (endpoints == null) {
            throw new DSCExecuteException("Static discovery was not configured");
        }

        return endpoints.stream()
                .filter(x -> includedAssetIDs.add(x.assetId()))
                .map(
                        x -> {
                            DSPFilter filter =
                                    new DSPFilter(
                                            "https://w3id.org/edc/v0.0.1/ns/id", null, x.assetId());
                            return (AccessRequest) new DSPRequest(filter, x.dspEndpoint());
                        })
                .toList();
    }

    @Override
    public List<GateRequest> convertToGateRequests(List<AccessResponse> accessResponses, Void empty)
            throws DSCExecuteException {

        Map<String, AccessResponse> tokenMap = new HashMap<>();

        for (AccessResponse response : accessResponses) {
            tokenMap.put(response.identifier().toString(), response);
        }

        return endpoints.stream()
                .map(
                        x ->
                                new GateRequest(
                                        tokenMap.get(x.assetId()).url(),
                                        tokenMap.get(x.assetId()).token(),
                                        x.interfaceType()))
                .toList();
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {

        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        Object endpointsObj = config.get("endpoints");

        ObjectMapper mapper = new ObjectMapper();

        if (endpointsObj instanceof Map<?, ?> endpointsMap) {

            this.endpoints =
                    endpointsMap.values().stream()
                            .map(
                                    value -> {
                                        return mapper.convertValue(value, ResultItem.class);
                                    })
                            .toList();

        } else {
            this.endpoints =
                    mapper.convertValue(endpointsObj, new TypeReference<List<ResultItem>>() {});
        }
    }
}
