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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponseFormat;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ConnectivityException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.faaast.client.interfaces.AASInterface;
import de.fraunhofer.iosb.ilt.faaast.client.interfaces.AASRepositoryInterface;
import de.fraunhofer.iosb.ilt.faaast.client.interfaces.SubmodelInterface;
import de.fraunhofer.iosb.ilt.faaast.client.interfaces.SubmodelRepositoryInterface;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.pf4j.Extension;

@Extension
public class GateImpl implements Gate {

    private static final Logger LOGGER = Logger.getLogger(GateImpl.class.getName());

    public GateImpl() {}

    private static final Pattern AAS_REPO_PATTERN = Pattern.compile("^aas[-_]repo.*");

    private static final Pattern AAS_PATTERN = Pattern.compile("^aas.*");

    private static final Pattern SUBMODEL_REPO_PATTERN = Pattern.compile("^submodel[-_]repo.*");

    private static final Pattern SUBMODEL_PATTERN = Pattern.compile("^submodel.*");

    enum InterfaceType {
        AAS_REPOSITORY,
        AAS,
        SUBMODEL_REPOSITORY,
        SUBMODEL,
        UNKNOWN;

        static InterfaceType from(String value) {
            if (value == null) return AAS_REPOSITORY;
            if (AAS_REPO_PATTERN.matcher(value).matches()) return AAS_REPOSITORY;
            if (AAS_PATTERN.matcher(value).matches()) return AAS;
            if (SUBMODEL_REPO_PATTERN.matcher(value).matches()) return SUBMODEL_REPOSITORY;
            if (SUBMODEL_PATTERN.matcher(value).matches()) return SUBMODEL;
            return UNKNOWN;
        }
    }

    private String normalizeUrl(String url) {
        // faaast-client needs urls without the /shells or /submodels suffix:

        if (url.endsWith("/shells")) {
            url = url.substring(0, url.length() - 7);
        } else if (url.endsWith("/submodels")) {
            url = url.substring(0, url.length() - 10);
        }
        return url;
    }

    @Override
    public GateResponse getData(GateRequest gateRequest, List<GateResponseFormat> desiredFormats) {

        List<AssetAdministrationShell> shells = new ArrayList<>();
        List<Submodel> submodels = new ArrayList<>();

        Map<String, List<String>> headers = new HashMap<>();
        try (TokenAuthenticatedHttpClient client =
                new TokenAuthenticatedHttpClient(gateRequest.token())) {

            String url = normalizeUrl(gateRequest.url());

            URI aasServerAddressUri = new URI(url);
            String interfaceType = null;

            Object metaInfo = gateRequest.metaInformation();
            if (metaInfo instanceof String metaString) {
                interfaceType = metaString;
            }

            if (interfaceType == null || AAS_REPO_PATTERN.matcher(interfaceType).matches()) {

                AASRepositoryInterface aasRepo =
                        new AASRepositoryInterface(aasServerAddressUri, client);
                shells.addAll(aasRepo.getAll());

            } else if (AAS_PATTERN.matcher(interfaceType).matches()) {
                AASInterface aasInterface = new AASInterface(aasServerAddressUri, client);
                shells.add(aasInterface.get());

            } else if (SUBMODEL_REPO_PATTERN.matcher(interfaceType).matches()) {

                SubmodelRepositoryInterface submodelRepo =
                        new SubmodelRepositoryInterface(aasServerAddressUri, client);
                submodels.addAll(submodelRepo.getAll());
            } else if (SUBMODEL_PATTERN.matcher(interfaceType).matches()) {
                SubmodelInterface submodelInterface =
                        new SubmodelInterface(aasServerAddressUri, client);
                submodels.add(submodelInterface.get());
            } else {

                LOGGER.log(Level.WARNING, "AAS Interface type \"{0}\" unknown.", interfaceType);
            }

            Environment environment =
                    new DefaultEnvironment.Builder()
                            .assetAdministrationShells(shells.isEmpty() ? null : shells)
                            .submodels(submodels.isEmpty() ? null : submodels)
                            .build();

            JsonSerializer serializer = new JsonSerializer();
            byte[] payload = serializer.write(environment).getBytes();

            return new GateResponse(200, GateResponseFormat.JSON, headers, payload, "");
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
        } catch (Exception exception) {
            LOGGER.severe("Unexpected exception: " + exception.getMessage());
        }
        return new GateResponse(500, GateResponseFormat.JSON, null, null, null);
    }
}
