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
package de.fraunhofer.iosb.ilt.dataspace_consumer.file_adapter_extension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class FileAdapter implements Adapter, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(FileAdapter.class);
    private String folderPath;

    /**
     * Creates a new FileAdapter instance.
     *
     * <p>No special initialization is required for this adapter.
     */
    public FileAdapter() {}

    private String payladTypeToExtension(ConverterPayloadType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case JSON -> "json";
            case XML -> "xml";
            case TEXT -> "txt";
            case BINARY -> "bin";
            case RDF -> "rdf.xml";
            case HTML -> "html";
            case CSV -> "csv";
            case PROTOBUF -> "proto";
            case AASX -> "aasx";
            case MULTIPART -> "multipart";
            case EMPTY -> "";
            case UNKNOWN -> "bin";
        };
    }

    private String getFileName(ConverterPayloadType type) {
        String extension = payladTypeToExtension(type);
        if (extension.equals("")) {
            return "payload";
        } else {
            return "payload." + extension;
        }
    }

    /**
     * Writes the payload contained in the given ConverterResponse to a file.
     *
     * <p>If the provided request or its payload is null, a warning is logged and the method returns
     * without throwing an exception. Otherwise the payload type and the UTF-8 decoded payload
     * contents are written to a file in the specified folder (folderPath property).
     *
     * @param request the ConverterResponse containing the payload to log
     * @throws DSCExecuteException declared by the Adapter interface; this implementation does not
     *     throw it under normal circumstances
     */
    @Override
    public void adapt(ConverterResponse request) throws DSCExecuteException {

        if (request == null || request.getPayload() == null) {
            LOG.warn("ConverterResponse oder Payload ist null");
            return;
        }

        Path path = Paths.get(folderPath, getFileName(request.getType()));

        LOG.info("=== FileAdapter ===");

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, request.getPayload());
            LOG.info("Payload saved in: {}", path);
        } catch (IOException e) {
            LOG.warn("IO exception: Failed to write payload to: {}", path);
        }
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.folderPath = config.get("folderPath").toString();
    }
}
