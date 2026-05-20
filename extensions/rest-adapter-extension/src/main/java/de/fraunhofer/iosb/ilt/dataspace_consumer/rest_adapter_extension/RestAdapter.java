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
package de.fraunhofer.iosb.ilt.dataspace_consumer.rest_adapter_extension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.Extension;

/**
 * REST adapter implementation that sends converted payloads to a configured HTTP endpoint.
 *
 * <p>The adapter expects to be configured with a map containing a "baseUrl" entry that points to
 * the target endpoint. When adapt(...) is called it issues an HTTP POST request with the converter
 * payload and sets the Content-Type header according to the payload type and optional encoding
 * provided by the converter response.
 */
@Extension
public class RestAdapter implements Adapter, Configurable {
    private String endpointUrl;

    /**
     * Public no-argument constructor required by the PF4J extension framework.
     *
     * <p>Keeping an explicit no-arg constructor improves clarity for reflection-based instantiation
     * and static analysis tools.
     */
    public RestAdapter() {
        // Intentionally empty: required by the PF4J extension framework which instantiates
        // extensions via reflection. Keeping an explicit no-arg constructor improves clarity
        // for static analysis tools (see SONAR java:S1186).
    }

    /**
     * Send the converted payload as an HTTP POST request to the configured endpoint.
     *
     * <p>The Content-Type header is derived from the converter response's payload type and optional
     * encoding. The method throws a DSCExecuteException if the request cannot be sent, the endpoint
     * URL is invalid, or the response status code is not in the 2xx range.
     *
     * @param request the converter response containing payload, payload type and optional encoding
     * @throws DSCExecuteException if an I/O error occurs, the endpoint URL is invalid, or the HTTP
     *     response status code is not successful (i.e. not in the 2xx range)
     */
    @Override
    public void adapt(ConverterResponse request) throws DSCExecuteException {
        try {

            URL url = new URI(endpointUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String contentType = getContentType(request.getType(), request.getEncoding());
            connection.setRequestProperty("Content-Type", contentType);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(request.getPayload());
            }

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new DSCExecuteException("REST request failed with status " + statusCode);
            }
        } catch (IOException e) {
            throw new DSCExecuteException("Failed to send REST request to " + endpointUrl, e);
        } catch (URISyntaxException e) {
            throw new DSCExecuteException("Invalid URI syntax: " + endpointUrl, e);
        }
    }

    private String getContentType(ConverterPayloadType type, Optional<String> encoding) {
        String baseType = type.getMediaType();
        String charset = encoding.map(enc -> "; charset=" + enc).orElse("");
        return baseType + charset;
    }

    /**
     * Set the adapter configuration.
     *
     * <p>The configuration map must contain a "baseUrl" entry whose value will be used as the HTTP
     * endpoint for outgoing requests. The method will throw an IllegalArgumentException if the
     * provided configuration map is null or if the required "baseUrl" entry is missing.
     *
     * @param config configuration map, must contain the key "baseUrl"
     * @throws IllegalArgumentException if config is null or does not contain the "baseUrl" key
     */
    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        if (!config.containsKey("baseUrl") || config.get("baseUrl") == null) {
            throw new IllegalArgumentException("Configuration must contain the 'baseUrl' entry");
        }

        this.endpointUrl = config.get("baseUrl").toString();
    }
}
