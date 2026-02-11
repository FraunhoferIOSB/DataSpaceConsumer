package de.fraunhofer.iosb.ilt.dataspace_consumer.simple_converter_extension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.AASXDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

public class GateResponsesToWrappedJson {

    private final JsonDeserializer jsonDeserializer = new JsonDeserializer();
    private final XmlDeserializer xmlDeserializer = new XmlDeserializer();
    private final JsonSerializer jsonSerializer = new JsonSerializer();
    private final ObjectMapper mapper = new ObjectMapper();

    public ConverterResponse wrapAllAsJsonObject(List<GateResponse> responses)
            throws UnsupportedOperationException {
        if (responses == null) {
            throw new UnsupportedOperationException("responses must not be null");
        }
        try {
            List<Environment> envs = new ArrayList<>();

            for (GateResponse r : responses) {
                if (r == null || r.getPayloadBytes() == null || r.getPayloadBytes().length == 0) {
                    continue;
                }
                Optional<GateResponseFormat> format = r.getFormat();
                if (format.isPresent()) {
                    Environment env = readAsEnvironment(format.get(), r.getPayloadBytes());
                    envs.add(env);
                }
            }

            byte[] envArrayBytes;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                jsonSerializer.write(out, envs.toArray(new Environment[0]));
                envArrayBytes = out.toByteArray();
            }

            ArrayNode envArrayNode = (ArrayNode) mapper.readTree(envArrayBytes);
            ObjectNode root = mapper.createObjectNode();
            root.set("environments", envArrayNode);

            byte[] wrapped = mapper.writeValueAsBytes(root);
            return new ConverterResponse(ConverterPayloadType.JSON, wrapped);
        } catch (UnsupportedOperationException e) {
            throw e;

        } catch (Exception e) {
            throw new DSCExecuteException("Failed to wrap GateResponses as JSON", e);
        }
    }

    private Environment readAsEnvironment(GateResponseFormat format, byte[] payload)
            throws IOException, InvalidFormatException, DeserializationException {
        return switch (format) {
            case JSON, JSON_LD -> readJson(payload);
            case XML -> readXml(payload);
            case AASX, AASX_XML, AASX_BINARY, AASX_PACKAGE -> readAasx(payload);
            case RDF_XML, RDF_TURTLE ->
                    throw new UnsupportedOperationException("RDF is not supported yet: " + format);

            case MULTIPART_FORM_DATA ->
                    throw new UnsupportedOperationException(
                            "multipart/form-data not supported yet (needs boundary parsing): "
                                    + format);

            case OCTET_STREAM ->
                    throw new UnsupportedOperationException(
                            "application/octet-stream not supported yet (unknown"
                                    + " container/encoding): "
                                    + format);
        };
    }

    private Environment readJson(byte[] payload) throws IOException, DeserializationException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(payload)) {
            return jsonDeserializer.read(in, Environment.class);
        }
    }

    private Environment readXml(byte[] payload) throws IOException, DeserializationException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(payload)) {
            return xmlDeserializer.read(in, StandardCharsets.UTF_8);
        }
    }

    private Environment readAasx(byte[] payload)
            throws IOException, DeserializationException, InvalidFormatException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(payload)) {
            AASXDeserializer deserializer = new AASXDeserializer(in);
            return deserializer.read();
        }
    }
}
