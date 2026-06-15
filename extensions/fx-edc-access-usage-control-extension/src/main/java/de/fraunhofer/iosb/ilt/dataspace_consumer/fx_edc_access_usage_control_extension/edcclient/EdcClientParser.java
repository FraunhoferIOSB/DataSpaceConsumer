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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.InitData;

/**
 * Parser utility used to parse EDC (Eclipse Dataspace Connector) related JSON responses and to
 * construct initial policy data required by the extension.
 *
 * <p>Provides a Jackson {@link ObjectMapper} instance and helper methods that centralize JSON
 * parsing and error handling for the extension's EDC client operations.
 */
public class EdcClientParser {

    private static final Logger LOGGER = Logger.getLogger(EdcClientParser.class.getName());
    private final ObjectMapper mapper;

    /** Create a new parser and initialize the internal {@link ObjectMapper}. */
    public EdcClientParser() {
        this.mapper = new ObjectMapper();
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

    /**
     * Returns the internal {@link ObjectMapper} used for JSON processing.
     *
     * @return the configured {@link ObjectMapper} instance
     */
    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * Execute a JSON parsing operation that may throw {@link JsonProcessingException} and convert
     * any such exception into a {@link DSCExecuteException} with a descriptive message containing
     * the provided request name.
     *
     * @param <T> the type returned by the parsing operation
     * @param parsingOperation a supplier that performs JSON parsing and returns a value
     * @param requestName a human-readable name of the request being parsed, used for logging and
     *     error messages
     * @return the result of the parsing operation
     * @throws DSCExecuteException if JSON parsing fails
     */
    public <T> T parseJson(JsonSupplier<T> parsingOperation, String requestName)
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

    /**
     * Parse an EDC discovery response payload to find an applicable policy for the local
     * participant and construct an {@link InitData} instance containing the policy JSON (as a
     * string) and the dataset id.
     *
     * <p>The method iterates the "dataset" array in the given JSON root, matches an element where
     * "@id" equals "id", and reads the first element of "hasPolicy". It then sets "odrl:target" to
     * the dataset id and "odrl:assigner" to the root's "participantId" before returning the policy
     * JSON.
     *
     * @param root the root JSON node of the discovery response
     * @return an {@link InitData} containing the prepared policy JSON string and dataset id
     * @throws DSCExecuteException if no applicable policy is found or input is malformed
     */
    public InitData parsePolicy(JsonNode root) {

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
