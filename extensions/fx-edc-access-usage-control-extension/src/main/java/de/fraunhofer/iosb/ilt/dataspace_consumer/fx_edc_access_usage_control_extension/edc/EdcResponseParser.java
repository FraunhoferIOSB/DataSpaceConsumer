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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.InitData;

public class EdcResponseParser {

    private static final Logger LOGGER = Logger.getLogger(EdcResponseParser.class.getName());
    private final ObjectMapper mapper;

    public EdcResponseParser() {
        mapper = new ObjectMapper();
    }

    @FunctionalInterface
    /**
     * Functional supplier which may throw a JsonProcessingException when called.
     *
     * @param <T> the type of the supplied value
     */
    public interface JsonSupplier<T> {
        T get() throws JsonProcessingException;
    }

    public <T> T parse(String responseBodyString, TypeReference<T> type, String requestName) {
        return parseJson(() -> mapper.readValue(responseBodyString, type), requestName);
    }

    public <T> T parse(String responseBodyString, Class<T> type, String requestName) {
        return parseJson(() -> mapper.readValue(responseBodyString, type), requestName);
    }

    private static <T> T parseJson(JsonSupplier<T> parsingOperation, String requestName)
            throws DSCExecuteException {
        try {
            T result = parsingOperation.get();
            LOGGER.log(Level.FINE, "successfully parsed {0} response", requestName);
            return result;
        } catch (JsonProcessingException e) {
            throw new DSCExecuteException(
                    "Exception on " + requestName + " response JSON parsing: : " + e.getMessage(),
                    e);
        }
    }

    public InitData policyFromCatalogResponse(String responseBodyString) {

        JsonNode root = parseJson(() -> mapper.readTree(responseBodyString), "policy parsing");

        JsonNode participantId = root.get("participantId");

        for (JsonNode element : root.get("dataset")) {

            JsonNode idNode = element.get("@id");

            JsonNode id2Node = element.get("id");

            if (idNode != null && idNode.asText().equals(id2Node.asText())) {
                ArrayNode policyNodes = (ArrayNode) element.get("hasPolicy");
                if (policyNodes != null) {
                    ObjectNode policy = (ObjectNode) policyNodes.get(0);

                    ObjectNode target = policy.objectNode();
                    target.set("@id", id2Node.deepCopy());
                    policy.set("odrl:target", target);

                    ObjectNode assigner = policy.objectNode();
                    assigner.set("@id", participantId.deepCopy());
                    policy.set("odrl:assigner", assigner);

                    return new InitData(
                            policy.toString(), id2Node.asText()); // return raw JSON string
                }
            }
        }

        throw new DSCExecuteException("No applicable policy found");
    }
}
