package de.fraunhofer.iosb.ilt.dataspace_consumer.simple_converter_extension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.Converter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterCapabilities;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterPayloadType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import org.pf4j.Extension;

@Extension
public class SimpleConverter implements Converter, Configurable {
    private Filter filter;

    public SimpleConverter() {}

    @Override
    public ConverterResponse convert(List<GateResponse> responses)
            throws UnsupportedOperationException, DSCExecuteException {
        switch (filter.operation()) {
            case FilterOperation.INDEX_AT:
                GateResponse response = getGateResponseOnIndex(responses);
                return new ConverterResponse(ConverterPayloadType.JSON, response.getPayloadBytes());

            case FilterOperation.MERGE:
                GateResponsesToWrappedJson merger = new GateResponsesToWrappedJson();
                return merger.wrapAllAsJsonObject(responses);
            default:
                throw new DSCExecuteException(
                        "Unsupported filter operation: " + filter.operation());
        }
    }

    private GateResponse getGateResponseOnIndex(List<GateResponse> responses) {
        int indexAt;
        try {
            indexAt = (int) filter.value();
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("Invalid Payload-Typ", e);
        }
        GateResponse response = responses.get(indexAt);
        Optional<GateResponseFormat> format = response.getFormat();
        if (format.isEmpty() || format.get() != GateResponseFormat.JSON) {
            throw new UnsupportedOperationException(
                    "Invalid response format: " + response.getFormat());
        }
        return response;
    }

    @Override
    public ConverterCapabilities getCapabilities() {
        return new ConverterCapabilities(List.of(GateResponseFormat.JSON));
    }

    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        Object opObj = config.get("operation");
        if (!(opObj instanceof String opStr)) {
            throw new IllegalArgumentException("Configuration field 'operation' must be a String");
        }

        FilterOperation operation;
        switch (opStr) {
            case "indexAt" -> operation = FilterOperation.INDEX_AT;
            case "merge" -> operation = FilterOperation.MERGE;
            default -> throw new IllegalArgumentException("Unknown filter operation: " + opStr);
        }

        Object value = config.get("value");
        this.filter = new Filter(operation, value);
    }
}
