package de.fraunhofer.iosb.ilt.dataspace_consumer.rest_adapter_extension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.Extension;

@Extension
public class RestAdapter implements Adapter, Configurable {
    private String endpointUrl;

    public RestAdapter() {}

    @Override
    public void adapt(ConverterResponse request) throws DSCExecuteException {
        try {
            URL url = new URL(endpointUrl);
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
        }
    }

    private String getContentType(ConverterPayloadType type, Optional<String> encoding) {
        String baseType = type.toString().toLowerCase();
        String charset = encoding.map(enc -> "; charset=" + enc).orElse("");
        return baseType + charset;
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        this.endpointUrl = config.get("baseUrl").toString();
    }
}
