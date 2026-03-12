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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.UnsupportedPayloadTypeException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.Converter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterCapabilities;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.ConverterResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery.Discovery;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateFormatNotSupportedException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.GateResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.extension.DSCPluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DSCExecutor {
    @Value("${concurrency.max-gate-requests:10}")
    private int MAX_CONCURRENT_GATE_REQUESTS;

    @Value("${concurrency.max-access-requests:10}")
    private int MAX_CONCURRENT_ACCESS_REQUESTS;

    private static final Logger LOGGER = LoggerFactory.getLogger(DSCExecutor.class);

    /** Registry for managing and caching loaded MX-Port plugins */
    private final DSCPluginRegistry pluginRegistry;

    /**
     * Constructs the MXPortExecutor with the plugin registry.
     *
     * @param pluginRegistry the MXPortPluginRegistry for accessing cached plugins
     */
    public DSCExecutor(DSCPluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

    /**
     * Executes the MX-Port workflow for the specified MX-Port configuration.
     *
     * <p>This method retrieves pre-loaded and cached plugins from the registry and executes all
     * configured plugin components in sequence: Discovery, Gate, Converter, and Adapter.
     *
     * @param mxPortName the name of the MX-Port configuration to execute
     * @throws DSCExecuteException if the plugins are not found or execution fails
     */
    public void execute(String mxPortName) throws DSCExecuteException {
        LOGGER.info("Starting MX-Port execution for: {}", mxPortName);

        // Retrieve cached plugins from registry
        DSCPluginRegistry.LoadedPlugins plugins = pluginRegistry.getPluginsForPort(mxPortName);

        @SuppressWarnings("rawtypes")
        Discovery discovery = plugins.getDiscovery();
        AccessAndUsageControl accessControl = plugins.getAccessAndUsageControl();
        Gate gate = plugins.getGate();
        Converter converter = plugins.getConverter();
        Adapter adapter = plugins.getAdapter();

        // Execute components in sequence
        LOGGER.info("Executing workflow components for MX-Port: {}", mxPortName);

        // Retrieve converter capabilities
        ConverterCapabilities converterCapabilities = converter.getCapabilities();
        LOGGER.info(
                "Converter capabilities retrieved: {}",
                converterCapabilities.getSupportedFormats());

        // 1. (Layer 5): Execute Discovery and obtain gate requests
        // uses 4. (layer 4): access control
        LOGGER.info("Executing discovery for MX-Port: {}", mxPortName);
        List<GateRequest> gateRequests = executeDiscoveryPhase(discovery, accessControl);

        // 3. (Layer 3): Execute Gate for each GateRequest in parallel using Virtual Threads
        LOGGER.info("Executing gate for MX-Port: {}", mxPortName);
        List<GateResponse> gateResponses =
                executeGatePhase(gate, gateRequests, converterCapabilities);

        // Check all gate responses successful before proceeding to conversion
        gateResponses.forEach(
                gateResponse -> {
                    // Any non-2xx status code is considered a failure for the gate request
                    if (gateResponse.getStatus() < 200 || gateResponse.getStatus() >= 300) {
                        throw new DSCExecuteException(
                                "Gate request failed with status code: "
                                        + gateResponse.getStatus());
                    }
                });

        // 4. (Layer 2): Execute Converter
        LOGGER.info("Executing converter for MX-Port: {}", mxPortName);
        ConverterResponse adapterRequest = converter.convert(gateResponses);

        // 5. (Layer 1): Execute Adapter
        LOGGER.info("Executing adapter for MX-Port: {}", mxPortName);
        try {
            adapter.adapt(adapterRequest);
        } catch (UnsupportedPayloadTypeException e) {
            throw new DSCExecuteException(e.getMessage(), e);
        }

        LOGGER.info("MX-Port execution completed for: {}", mxPortName);
    }

    /**
     * Executes the Discovery phase (Layer 5) and retrieves the gate requests.
     *
     * <p>This method orchestrates the discovery workflow including:
     *
     * <ul>
     *   <li>Obtaining the discovery access request
     *   <li>Retrieving access information for discovery
     *   <li>Executing the discovery process
     *   <li>Retrieving access information for each discovered resource in parallel
     *   <li>Converting discovered information into gate requests
     * </ul>
     *
     * @param discovery the discovery plugin instance (raw type due to plugin system)
     * @param accessControl the access and usage control plugin (raw type due to plugin system)
     * @return a list of GateRequest objects to be processed by the Gate component
     * @throws DSCExecuteException if negotiation fails or the discovery process encounters an error
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<GateRequest> executeDiscoveryPhase(
            Discovery discovery, AccessAndUsageControl accessControl) throws DSCExecuteException {
        AccessRequest discoveryAccessRequest = discovery.getDiscoveryAccessRequest();

        AccessResponse discoverAccessInfos =
                accessControl.retrieveAccessInformation(discoveryAccessRequest);

        Object discoveredInfos = discovery.discover(discoverAccessInfos);

        List<AccessRequest> gateAccessRequests = discovery.getGateAccessRequests(discoveredInfos);

        // Retrieve access responses in parallel using virtual threads
        List<AccessResponse> gateAccessResponses =
                retrieveAccessInfosInParallel(accessControl, gateAccessRequests);

        return discovery.convertToGateRequests(gateAccessResponses, discoveredInfos);
    }

    /**
     * Retrieves access information for multiple access requests in parallel using virtual threads.
     *
     * <p>This method executes AccessAndUsageControl.retrieveAccessInformation for each provided
     * access request concurrently, improving performance when handling multiple resources.
     *
     * @param accessControl the access and usage control plugin (raw type due to plugin system)
     * @param accessRequests the list of access requests to process
     * @return a list of AccessResponse objects corresponding to each access request
     * @throws DSCExecuteException if any access request fails during processing
     */
    private List<AccessResponse> retrieveAccessInfosInParallel(
            AccessAndUsageControl accessControl, List<AccessRequest> accessRequests)
            throws DSCExecuteException {

        LOGGER.info(
                "Executing access requests with "
                        + MAX_CONCURRENT_ACCESS_REQUESTS
                        + " concurrent threads");
        final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_ACCESS_REQUESTS);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<AccessResponse>> futures = new ArrayList<>();

            for (AccessRequest accessRequest : accessRequests) {
                Future<AccessResponse> future =
                        executor.submit(
                                () -> {
                                    semaphore.acquire();
                                    try {
                                        return accessControl.retrieveAccessInformation(
                                                accessRequest);
                                    } finally {
                                        semaphore.release();
                                    }
                                });
                futures.add(future);
            }

            // Collect results
            List<AccessResponse> accessResponses = new ArrayList<>();
            for (Future<AccessResponse> future : futures) {
                accessResponses.add(future.get());
            }
            return accessResponses;
        } catch (InterruptedException | ExecutionException e) {
            throw new DSCExecuteException("Error retrieving access info: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the Gate phase (Layer 3) and retrieves data from multiple gate requests in parallel.
     *
     * <p>This method processes each GateRequest concurrently using virtual threads to optimize
     * performance when retrieving data from multiple sources. Each gate request is executed with
     * the supported formats provided by the converter.
     *
     * @param gate the gate plugin instance
     * @param gateRequests the list of gate requests to process
     * @param converterCapabilities the converter capabilities containing supported formats
     * @return a list of GateResponse objects corresponding to each gate request
     * @throws DSCExecuteException if any gate request fails or encounters a format not supported
     *     exception
     */
    private List<GateResponse> executeGatePhase(
            Gate gate, List<GateRequest> gateRequests, ConverterCapabilities converterCapabilities)
            throws DSCExecuteException {

        LOGGER.info(
                "Executing gate requests with "
                        + MAX_CONCURRENT_GATE_REQUESTS
                        + " concurrent threads");
        final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_GATE_REQUESTS);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<GateResponse>> futures = new ArrayList<>();

            for (GateRequest gateRequest : gateRequests) {
                LOGGER.debug("Processing GateRequest: {}", gateRequest);

                Future<GateResponse> future =
                        executor.submit(
                                () -> {
                                    semaphore.acquire();
                                    try {
                                        return gate.getData(
                                                gateRequest,
                                                converterCapabilities.getSupportedFormats());
                                    } catch (GateFormatNotSupportedException e) {
                                        throw new DSCExecuteException(e.getMessage(), e);
                                    } finally {
                                        semaphore.release();
                                    }
                                });
                futures.add(future);
            }

            // Collect results
            List<GateResponse> gateResponses = new ArrayList<>();
            for (Future<GateResponse> future : futures) {
                gateResponses.add(future.get());
            }
            return gateResponses;
        } catch (InterruptedException | ExecutionException e) {
            throw new DSCExecuteException("Error executing gate requests: " + e.getMessage(), e);
        }
    }
}
