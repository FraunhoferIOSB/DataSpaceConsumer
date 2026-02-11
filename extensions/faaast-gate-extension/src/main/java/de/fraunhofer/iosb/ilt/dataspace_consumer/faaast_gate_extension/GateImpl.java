package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ConnectivityException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.StatusCodeException;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.pf4j.Extension;

@Extension
public class GateImpl implements Gate {

    private static final Logger LOGGER = Logger.getLogger(GateImpl.class.getName());

    public GateImpl() {}

    @Override
    public GateResponse getData(GateRequest gateRequest, List<GateResponseFormat> desiredFormats) {

        Map<String, List<String>> headers = new HashMap<>();
        try (HttpClient client = new TokenAuthenticatedHttpClient(gateRequest.token())) {

            URI aasServerAddressUri = new URI(gateRequest.url());
            List<Submodel> submodels =
                    SubmodelHTTPFetcher.getAllSubmodels(aasServerAddressUri, client);
            GateResponsePayload payload = new GateResponsePayload(submodels);

            return new GateResponse(
                    200, GateResponseFormat.JSON, headers, payload.getAsBytes(), "");
        } catch (JsonProcessingException exception) {
            LOGGER.severe("Failed to process JSON: " + exception.getMessage());

        } catch (URISyntaxException exception) {
            LOGGER.severe("Invalid AAS server URI: " + exception.getMessage());

        } catch (ConnectivityException exception) {
            LOGGER.severe(
                    "Connectivity error while contacting AAS server: " + exception.getMessage());

        } catch (StatusCodeException exception) {
            LOGGER.severe(
                    "Received unexpected status code from AAS server: " + exception.getMessage());
        }
        return new GateResponse(500, GateResponseFormat.JSON, null, null, null);
    }
}
