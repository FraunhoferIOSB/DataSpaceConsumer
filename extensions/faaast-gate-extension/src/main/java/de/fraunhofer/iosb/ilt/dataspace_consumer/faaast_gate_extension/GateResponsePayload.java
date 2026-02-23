package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

public class GateResponsePayload {

    private String json;
    private byte[] bytes;

    public GateResponsePayload(List<Submodel> submodels) throws SerializationException {

        JsonSerializer serializer = new JsonSerializer();
        json = serializer.writeList(submodels);
        bytes = json.getBytes(StandardCharsets.UTF_8);
    }

    public String getAsString() {
        return json;
    }

    public byte[] getAsBytes() {
        return bytes;
    }
}
