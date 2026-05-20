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
import java.nio.charset.StandardCharsets;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.pf4j.Extension;

@Extension
/**
 * Implementation of the Gate API that fetches Asset Administration Shells (AAS) and Submodels from
 * a remote FA³ST server using the faaast-client library.
 *
 * <p>This class is exposed as a PF4J extension and implements {@link
 * de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate} so it can be discovered and invoked by
 * the DataSpaceConsumer framework.
 *
 * <p>The Gate determines which FA³ST interface to call based on the GateRequest.metaInformation()
 * value. Recognized interface identifiers are those starting with "aas-repo", "aas",
 * "submodel-repo" and "submodel" (case-insensitive). If metaInformation is null or matches the AAS
 * repository pattern, all shells are requested from the AAS repository endpoint. The returned
 * AAS/Submodel objects are serialized to JSON and returned as the GateResponse payload.
 */
public class GateImpl implements Gate {

    private static final Logger LOGGER = Logger.getLogger(GateImpl.class.getName());

    /**
     * Public no-argument constructor required by the PF4J extension framework.
     *
     * <p>PF4J instantiates extensions via reflection; keeping an explicit no-arg constructor
     * improves clarity for static analysis tools and makes the extension lifecycle explicit.
     */
    public GateImpl() {
        // Intentionally empty: required by the PF4J extension framework which instantiates
        // extensions via reflection. Keeping an explicit no-arg constructor improves clarity
        // for static analysis tools (see SONAR java:S1186).
    }

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

    /**
     * Fetch data from a FA³ST AAS server and return it as a GateResponse.
     *
     * <p>The method inspects {@code gateRequest.metaInformation()} to decide which FA³ST interface
     * to call. Supported meta information values (case insensitive) are:
     *
     * <ul>
     *   <li>"aas-repo*" - fetch all Asset Administration Shells from an AAS repository
     *   <li>"aas*" - fetch a single Asset Administration Shell
     *   <li>"submodel-repo*" - fetch all Submodels from a Submodel repository
     *   <li>"submodel*" - fetch a single Submodel
     * </ul>
     *
     * If {@code metaInformation} is {@code null} the implementation defaults to treating the
     * endpoint as an AAS repository and attempts to fetch all shells.
     *
     * <p>The retrieved AAS/Submodel objects are serialized to JSON using the aas4j {@link
     * JsonSerializer} and returned with a 200 status code on success. If any error occurs a 500
     * response with empty payload is returned and the exception is logged.
     *
     * @param gateRequest the request describing the target URL and authentication token
     * @param desiredFormats a list of desired response formats (currently ignored; this
     *     implementation always returns JSON)
     * @return a {@link GateResponse} containing the serialized environment in JSON
     */
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
                interfaceType = metaString.toLowerCase();
            }

            if (interfaceType == null || AAS_REPO_PATTERN.matcher(interfaceType).matches()) {

                AASRepositoryInterface aasRepo =
                        new AASRepositoryInterface(aasServerAddressUri, client);
                shells.addAll(aasRepo.get(PagingInfo.ALL).getContent());

            } else if (AAS_PATTERN.matcher(interfaceType).matches()) {
                AASInterface aasInterface = new AASInterface(aasServerAddressUri, client);
                shells.add(aasInterface.get());

            } else if (SUBMODEL_REPO_PATTERN.matcher(interfaceType).matches()) {

                SubmodelRepositoryInterface submodelRepo =
                        new SubmodelRepositoryInterface(aasServerAddressUri, client);
                submodels.addAll(submodelRepo.get(PagingInfo.ALL).getContent());

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
            byte[] payload = serializer.write(environment).getBytes(StandardCharsets.UTF_8);

            return new GateResponse(200, GateResponseFormat.JSON, headers, payload, "");
        } catch (SerializationException exception) {
            LOGGER.log(Level.SEVERE, "Failed to process JSON", exception);
        } catch (URISyntaxException exception) {
            LOGGER.log(Level.SEVERE, "Invalid AAS server URI", exception);
        } catch (ConnectivityException exception) {
            LOGGER.log(Level.SEVERE, "Connectivity error while contacting AAS server", exception);
        } catch (StatusCodeException exception) {
            LOGGER.log(Level.SEVERE, "Received unexpected status code from AAS server", exception);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Unexpected exception", exception);
        }
        return new GateResponse(500, GateResponseFormat.JSON, null, null, null);
    }
}
