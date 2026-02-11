package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

public class GateResponsePayload {

    private String json;
    private byte[] bytes;

    public GateResponsePayload(List<Submodel> submodels) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        json = mapper.writeValueAsString(submodels);
        bytes = json.getBytes(StandardCharsets.UTF_8);
    }

    public String getAsString() {
        return json;
    }

    public byte[] getAsBytes() {
        return bytes;
    }
}
