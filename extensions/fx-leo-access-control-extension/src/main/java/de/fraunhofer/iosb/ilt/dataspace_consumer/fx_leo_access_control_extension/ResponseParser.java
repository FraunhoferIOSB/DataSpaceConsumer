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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;

public class ResponseParser {

    private static final Logger LOGGER = Logger.getLogger(ResponseParser.class.getName());
    private final ObjectMapper mapper;

    public ResponseParser() {
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

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

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
}
