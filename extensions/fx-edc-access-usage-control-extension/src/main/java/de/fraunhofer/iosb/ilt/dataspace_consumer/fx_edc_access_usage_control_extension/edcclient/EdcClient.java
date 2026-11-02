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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient;

import static de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.template.EdcEndpointTemplates.getTransferProcessStatus;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPFilter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.AuthorizationContext;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.InitData;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.EdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.IdResponseDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.InitNegotiationDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.NegotiationStateDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.dto.TransferProcessDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.http.HttpClient;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.template.EdcEndpointTemplates;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.template.EdcRequestTemplates;

/**
 * HTTP client for interacting with an EDC (Eclipse DataSpace Connector) management API.
 *
 * <p>This class provides convenience methods to query the catalog for policies, initiate contract
 * negotiations, retrieve available EDRs, fetch access tokens (EDR) and poll the negotiation state.
 * It handles HTTP requests, response validation and JSON parsing via {@code EdcClientParser}.
 */
public class EdcClient {

    private final String baseURL;
    private final String counterPartyId;
    private final String counterPartyAddress;
    private final EdcClientParser parser;
    private final HttpClient httpClient;

    /**
     * Create a new EdcClient instance.
     *
     * @param baseURL base URL of the EDC management API (must not be null)
     * @param counterPartyId identifier of the counter-party (must not be null)
     * @param counterPartyAddress address/URL of the counter-party connector (must not be null)
     * @param apiKey API key used for authenticating against the EDC management API (must not be
     *     null)
     */
    public EdcClient(
            String baseURL, String counterPartyId, String counterPartyAddress, String apiKey) {
        this.httpClient = new HttpClient(apiKey);
        this.baseURL = Objects.requireNonNull(baseURL);
        this.counterPartyId = Objects.requireNonNull(counterPartyId);
        this.counterPartyAddress = Objects.requireNonNull(counterPartyAddress);
        this.parser = new EdcClientParser();
    }

    private static final Logger LOGGER = Logger.getLogger(EdcClient.class.getName());

    /**
     * Fetches the policy (InitData) for the supplied DSP request by querying the EDC catalog.
     *
     * <p>The method builds a catalog query from the filter contained in the {@code dspRequest} and
     * sends it to the EDC management API. The response is parsed into an {@link InitData}
     * representation.
     *
     * @param dspRequest request containing DSP filters used for the catalog query
     * @return parsed {@link InitData} describing the policy and any relevant metadata
     * @throws DSCExecuteException on HTTP errors, timeouts or JSON parsing failures
     */
    public InitData fetchPolicy(DSPRequest dspRequest) throws DSCExecuteException {
        DSPFilter filter = dspRequest.filters();
        String requestBodyString =
                EdcRequestTemplates.catalogRequest(
                        counterPartyAddress, counterPartyId, filter.key(), filter.value());
        String responseBodyString =
                httpClient.post(EdcEndpointTemplates.requestCatalog(baseURL), requestBodyString);
        return parser.parsePolicy(responseBodyString);
    }

    /**
     * Initiates a contract negotiation at the counter-party using the provided init data.
     *
     * <p>The contained policy from {@code initData} will be sent to the counter-party via the EDC
     * contract negotiation endpoint. The initial negotiation response is parsed into an {@link
     * InitNegotiationDTO} containing at least the negotiation id.
     *
     * @param initData initialization data containing the policy to negotiate
     * @return {@link InitNegotiationDTO} with information about the initiated negotiation
     * @throws DSCExecuteException on HTTP errors, timeouts or JSON parsing failures
     */
    public InitNegotiationDTO initiateNegotiation(InitData initData) throws DSCExecuteException {
        String requestBodyString =
                EdcRequestTemplates.contractNegotiation(counterPartyAddress, initData.policy());
        String responseBodyString =
                httpClient.post(
                        EdcEndpointTemplates.initiateContractNegotiation(baseURL),
                        requestBodyString);
        InitNegotiationDTO initNegotiationDTO = parse(responseBodyString, InitNegotiationDTO.class);

        logFine("negotiation", initNegotiationDTO);

        return initNegotiationDTO;
    }

    /**
     * Starts a transfer process at the provider data plane.
     *
     * @param assetId asset identifier to query EDRs for
     * @param agreementId agreement id of the contract which the asset is bound to.
     * @return transfer process ID
     * @throws DSCExecuteException starting transfer process failed.
     */
    public String startTransferProcess(String assetId, String agreementId) {
        String requestBodyString =
                EdcRequestTemplates.transferProcessRequest(
                        assetId, agreementId, counterPartyAddress, counterPartyId);
        String responseBodyString =
                httpClient.post(
                        EdcEndpointTemplates.initiateTransferProcess(baseURL), requestBodyString);
        IdResponseDTO idResponseDTO = parse(responseBodyString, IdResponseDTO.class);

        var future =
                waitForAgreement(
                        () ->
                                parse(
                                        httpClient.get(
                                                getTransferProcessStatus(
                                                        baseURL, idResponseDTO.id())),
                                        TransferProcessDTO.class));

        try {
            return future.get().id();
        } catch (InterruptedException | ExecutionException e) {
            throw new DSCExecuteException("Waiting for transfer process to start failed", e);
        }
    }

    /**
     * Fetches an EDR (access token) for the given transfer process id.
     *
     * @param transferProcessId transfer process identifier used to locate the token
     * @return parsed {@link EdrDTO} containing the access token and metadata
     * @throws DSCExecuteException on HTTP or parsing errors (thrown as runtime exception by
     *     helpers)
     */
    public EdrDTO fetchToken(String transferProcessId) {
        String responseBodyString =
                httpClient.get(EdcEndpointTemplates.requestAccessToken(baseURL, transferProcessId));
        return parse(responseBodyString, EdrDTO.class);
    }

    /**
     * Retrieves the current negotiation state for the negotiation referenced in the {@link
     * AuthorizationContext}.
     *
     * @param context authorization context containing the negotiation id to poll
     * @return {@link NegotiationStateDTO} representing the current negotiation state
     * @throws DSCExecuteException on HTTP or parsing errors (thrown as runtime exception by
     *     helpers)
     */
    public NegotiationStateDTO getNegotiationState(AuthorizationContext context) {
        String responseBodyString =
                httpClient.get(
                        EdcEndpointTemplates.getNegotiationStatus(
                                baseURL, context.negotiationId()));
        NegotiationStateDTO negotiationStateDTO =
                parse(responseBodyString, NegotiationStateDTO.class);
        logFine("state", negotiationStateDTO);
        return negotiationStateDTO;
    }

    private Future<TransferProcessDTO> waitForAgreement(
            Supplier<TransferProcessDTO> stateSupplier) {
        Objects.requireNonNull(stateSupplier, "stateSupplier");

        FutureTask<TransferProcessDTO> task =
                new FutureTask<>(
                        () -> {
                            long delayMillis = 500;
                            final long maxDelayMillis = 5_000;

                            while (true) {
                                if (Thread.currentThread().isInterrupted()) {
                                    throw new InterruptedException("Agreement wait interrupted");
                                }

                                TransferProcessDTO state = stateSupplier.get();

                                if (state.succeeded()) {
                                    return state;
                                } else if (state.isFinished()) {
                                    throw new DSCExecuteException(
                                            String.format(
                                                    "Starting transfer process failed: %s", state));
                                }

                                TimeUnit.MILLISECONDS.sleep(delayMillis);
                                delayMillis = Math.min(delayMillis * 2, maxDelayMillis);
                            }
                        });

        new Thread(task, "agreement-wait").start();
        return task;
    }

    private <T> T parse(String toParse, Class<T> clazz) {
        return parser.parseJson(toParse, clazz);
    }

    private void logFine(String message, Object objectToLog) {
        LOGGER.fine(String.format("%s: %s", message, objectToLog));
    }
}
