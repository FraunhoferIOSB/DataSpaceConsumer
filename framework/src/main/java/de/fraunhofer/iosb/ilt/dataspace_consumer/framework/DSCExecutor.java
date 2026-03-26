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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.springframework.stereotype.Service;

@Service
public class DSCExecutor {

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

    public void execute(String mxPortName, long timeout) throws DSCExecuteException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Future<?> future = executor.submit(() -> execute(mxPortName));

        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new DSCExecuteException("MX-Port execution timed out", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DSCExecuteException d) {
                throw d;
            }
            throw new DSCExecuteException(cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DSCExecuteException("Execution interrupted", e);
        }
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
        int maxGateRequests = plugins.getExecutionConfig().getMaxGateRequests();

        // Execute components in sequence
        LOGGER.info("Executing workflow components for MX-Port: {}", mxPortName);

        // Retrieve converter capabilities
        ConverterCapabilities converterCapabilities = converter.getCapabilities();
        LOGGER.info(
                "Converter capabilities retrieved: {}",
                converterCapabilities.getSupportedFormats());

        // 1. (Layer 5): Execute Discovery phase
        // uses 4. (layer 4): access control
        LOGGER.info("Executing discovery for MX-Port: {}", mxPortName);
        DiscoveryResult discoveryResult = executeDiscoveryPhase(discovery, accessControl);

        // 3. (Layer 3): Execute batched Gate phase for each access request in parallel
        LOGGER.info("Executing gate for MX-Port: {}", mxPortName);
        List<GateResponse> gateResponses =
                executeBatchedGatePhase(
                        discovery,
                        accessControl,
                        gate,
                        converterCapabilities,
                        maxGateRequests,
                        discoveryResult.gateAccessRequests,
                        discoveryResult.discoveredInfos);

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
     * Represents the result of the discovery phase containing access requests and discovered info.
     */
    private static class DiscoveryResult {
        final List<AccessRequest> gateAccessRequests;
        final Object discoveredInfos;

        DiscoveryResult(List<AccessRequest> gateAccessRequests, Object discoveredInfos) {
            this.gateAccessRequests = gateAccessRequests;
            this.discoveredInfos = discoveredInfos;
        }
    }

    /**
     * Executes the Discovery phase (Layer 5) and retrieves access requests.
     *
     * <p>This method orchestrates the initial discovery workflow including:
     *
     * <ul>
     *   <li>Obtaining the discovery access request
     *   <li>Retrieving access information for discovery
     *   <li>Executing the discovery process
     *   <li>Extracting access requests for each discovered resource
     * </ul>
     *
     * @param discovery the discovery plugin instance (raw type due to plugin system)
     * @param accessControl the access and usage control plugin (raw type due to plugin system)
     * @return a DiscoveryResult containing access requests and discovered information
     * @throws DSCExecuteException if negotiation fails or the discovery process encounters an error
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private DiscoveryResult executeDiscoveryPhase(
            Discovery discovery, AccessAndUsageControl accessControl) throws DSCExecuteException {
        AccessRequest discoveryAccessRequest = discovery.getDiscoveryAccessRequest();

        AccessResponse discoverAccessInfos =
                accessControl.retrieveAccessInformation(discoveryAccessRequest);

        Object discoveredInfos = discovery.discover(discoverAccessInfos);

        List<AccessRequest> gateAccessRequests = discovery.getGateAccessRequests(discoveredInfos);

        return new DiscoveryResult(gateAccessRequests, discoveredInfos);
    }

    /**
     * Executes the Gate phase in batches to manage concurrent requests efficiently.
     *
     * <p>This method processes access requests in batches of maxGateRequests:
     *
     * <ul>
     *   <li>For each batch, retrieve access responses in parallel
     *   <li>Then convert the responses to gate requests
     *   <li>Then execute gate.getData for the gate requests in parallel
     *   <li>Repeat until all access requests are processed
     * </ul>
     *
     * @param discovery the discovery plugin instance (raw type due to plugin system)
     * @param accessControl the access and usage control plugin (raw type due to plugin system)
     * @param gate the gate plugin instance
     * @param converterCapabilities the converter capabilities containing supported formats
     * @param maxGateRequests the maximum number of concurrent gate requests for this MX-Port
     * @param gateAccessRequests the list of access requests to process in batches
     * @param discoveredInfos the discovered information from discovery
     * @return a list of all GateResponse objects from all batches
     * @throws DSCExecuteException if batch processing fails
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<GateResponse> executeBatchedGatePhase(
            Discovery discovery,
            AccessAndUsageControl accessControl,
            Gate gate,
            ConverterCapabilities converterCapabilities,
            int maxGateRequests,
            List<AccessRequest> gateAccessRequests,
            Object discoveredInfos)
            throws DSCExecuteException {

        List<GateResponse> allGateResponses = new ArrayList<>();

        // Process access requests in batches
        for (int i = 0; i < gateAccessRequests.size(); i += maxGateRequests) {
            int endIndex = Math.min(i + maxGateRequests, gateAccessRequests.size());
            List<AccessRequest> batch = gateAccessRequests.subList(i, endIndex);

            LOGGER.debug(
                    "Processing batch {} of access requests (size: {})",
                    (i / maxGateRequests) + 1,
                    batch.size());

            // Step 1: Retrieve access responses for the entire batch in parallel
            List<AccessResponse> batchAccessResponses =
                    retrieveAccessInfosInParallel(accessControl, batch);

            // Step 2: Convert access responses to gate requests
            List<GateRequest> batchGateRequests =
                    discovery.convertToGateRequests(batchAccessResponses, discoveredInfos);

            // Step 3: Execute gate.getData for the gate requests in parallel
            List<GateResponse> batchGateResponses =
                    executeGatePhase(
                            gate, batchGateRequests, converterCapabilities, maxGateRequests);

            allGateResponses.addAll(batchGateResponses);
        }

        return allGateResponses;
    }

    /**
     * Retrieves access information for multiple access requests in parallel using virtual threads.
     *
     * <p>This method executes AccessAndUsageControl.retrieveAccessInformation for each provided
     * access request concurrently. The batch size is already limited by the caller
     * (executeBatchedGatePhase), resulting in efficient resource usage.
     *
     * @param accessControl the access and usage control plugin (raw type due to plugin system)
     * @param accessRequests the list of access requests to process (batch size is pre-limited)
     * @return a list of AccessResponse objects corresponding to each access request
     * @throws DSCExecuteException if any access request fails during processing
     */
    private List<AccessResponse> retrieveAccessInfosInParallel(
            AccessAndUsageControl accessControl, List<AccessRequest> accessRequests)
            throws DSCExecuteException {

        LOGGER.debug("Executing {} access requests in parallel", accessRequests.size());

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<AccessResponse>> futures = new ArrayList<>();

            for (AccessRequest accessRequest : accessRequests) {
                Future<AccessResponse> future =
                        executor.submit(
                                () -> accessControl.retrieveAccessInformation(accessRequest));
                futures.add(future);
            }

            // Collect results
            List<AccessResponse> accessResponses = new ArrayList<>();
            for (Future<AccessResponse> future : futures) {
                try {
                    accessResponses.add(future.get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    throw new DSCExecuteException(
                            "Error retrieving access info: " + cause.getMessage(), cause);
                }
            }
            return accessResponses;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DSCExecuteException("Execution interrupted: " + e.getMessage(), e);
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
     * @param maxGateRequests the maximum number of concurrent gate requests
     * @return a list of GateResponse objects corresponding to each gate request
     * @throws DSCExecuteException if any gate request fails or encounters a format not supported
     *     exception
     */
    private List<GateResponse> executeGatePhase(
            Gate gate,
            List<GateRequest> gateRequests,
            ConverterCapabilities converterCapabilities,
            int maxGateRequests)
            throws DSCExecuteException {

        LOGGER.debug("Executing gate requests with " + maxGateRequests + " concurrent threads");
        final Semaphore semaphore = new Semaphore(maxGateRequests);

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
                try {
                    gateResponses.add(future.get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    throw new DSCExecuteException(
                            "Error executing gate requests: " + cause.getMessage(), cause);
                }
            }
            return gateResponses;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DSCExecuteException("Execution interrupted: " + e.getMessage(), e);
        }
    }
}
