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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.generic.GenericRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;

/**
 * Factory that creates AccessRequest instances from discovery result items.
 *
 * <p>This class converts a list of {@link ResultItem} objects (for example, discovered resources
 * with hrefs) into a list of {@link AccessRequest} objects. Each ResultItem's href value is stored
 * in a JSON text node and wrapped into a {@link GenericRequest}.
 */
public class DiscoveryRequestFactory {

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Create gate access requests from discovery result items.
     *
     * <p>For every {@link ResultItem} in the provided list this method creates a JSON text node
     * containing the item's href and constructs a {@link GenericRequest} that is returned as an
     * {@link AccessRequest}.
     *
     * @param items the discovery result items to convert; must not be {@code null}
     * @return a list of {@link AccessRequest} instances created from the input items; never {@code
     *     null}
     * @throws DSCExecuteException if the conversion cannot be performed. Note: the current
     *     implementation does not raise an exception in normal operation, but the signature keeps
     *     the exception for API compatibility.
     */
    public List<AccessRequest> getGateAccessRequests(List<ResultItem> items)
            throws DSCExecuteException {

        return items.stream()
                .map(
                        x -> {
                            JsonNode node = mapper.getNodeFactory().textNode(x.href());
                            return (AccessRequest) new GenericRequest(node);
                        })
                .toList();
    }
}
