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
package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ConnectivityException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.StatusCodeException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
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
        } catch (SerializationException exception) {
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
