package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;

/**
 * Executor for DSP Access and Usage Control with negotiation support.
 *
 * @param <C> the negotiation/context type used by the access control implementation
 */
public class DSPExcecuter<C> {
    private final DSPAccessAndUsageControl<C> accessAndUsageControl;

    private final long pollingIntervalMillis = 1000;

    private final int maxRetries = 10;

    /**
     * Create an executor using the provided access and usage control implementation.
     *
     * @param accessAndUsageControl the DSP access and usage control implementation
     */
    public DSPExcecuter(DSPAccessAndUsageControl<C> accessAndUsageControl) {
        this.accessAndUsageControl = accessAndUsageControl;
    }

    /**
     * Validates configuration parameters to prevent runtime errors from misconfiguration.
     *
     * <p>Ensures that polling interval and max retries are positive values. In production
     * environments, invalid configuration (e.g., zero or negative values) could lead to infinite
     * loops or immediate failure. This validation provides early error detection.
     *
     * @throws DSCExecuteException if configuration values are invalid
     */
    private void validateConfiguration() throws DSCExecuteException {
        if (pollingIntervalMillis <= 0) {
            throw new DSCExecuteException(
                    "Invalid configuration: pollingIntervalMillis must be positive, got: "
                            + pollingIntervalMillis);
        }
        if (maxRetries <= 0) {
            throw new DSCExecuteException(
                    "Invalid configuration: maxRetries must be positive, got: " + maxRetries);
        }
    }

    /**
     * Validates that the provided access request uses a supported sub-protocol type.
     *
     * <p>This explicit validation step improves code clarity and makes it easier for LLM-based
     * systems to understand the control flow and handle errors appropriately.
     *
     * @param request the access request containing the sub-protocol type to validate
     * @throws DSCExecuteException if the sub-protocol type is not supported
     */
    private void validateProtocolSupport(AccessRequest request) throws DSCExecuteException {
        if (!accessAndUsageControl
                .getSupportedSubProtocolTypes()
                .contains(request.getSubProtocolType())) {
            throw new DSCExecuteException("Unsupported Protocol: " + request.getSubProtocolType());
        }
    }

    /**
     * Performs synchronous polling to wait for negotiation finalization.
     *
     * <p>This method blocks the current thread (efficiently on Virtual Threads) and polls the
     * access control layer until either:
     *
     * <ul>
     *   <li>Negotiation is finalized (success)
     *   <li>Max retries are exhausted (failure)
     *   <li>Thread is interrupted (error)
     * </ul>
     *
     * <p>Configuration parameters are passed explicitly to enable clear audit trails and support
     * for adaptive/dynamic timeout strategies. This imperative, linear approach is optimized for
     * LLM tool-calling and easier to reason about.
     *
     * @param context the context object from negotiation initialization
     * @param pollingIntervalMs the interval (in milliseconds) between negotiation status checks
     * @param retryCount the maximum number of polling attempts
     * @throws DSCExecuteException if negotiation fails, times out, or if parameters are invalid
     */
    private void performPolling(C context, long pollingIntervalMs, int retryCount)
            throws DSCExecuteException {
        // Validate parameters to prevent runtime errors from misconfiguration
        if (pollingIntervalMs <= 0) {
            throw new DSCExecuteException(
                    "Invalid polling interval: must be positive, got: " + pollingIntervalMs);
        }
        if (retryCount <= 0) {
            throw new DSCExecuteException(
                    "Invalid retry count: must be positive, got: " + retryCount);
        }

        for (int attempt = 1; attempt <= retryCount; attempt++) {
            // Check if negotiation is finalized
            if (accessAndUsageControl.isNegotiationFinalized(context)) {
                return; // Success
            }

            // Skip sleep on last failed attempt
            if (attempt < retryCount) {
                try {
                    // Block current thread efficiently (Virtual Threads make this optimal)
                    Thread.sleep(pollingIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new DSCExecuteException("Negotiation polling interrupted", e);
                }
            }
        }

        // Max retries exhausted
        throw new DSCExecuteException(
                "Negotiation timeout: not finalized after "
                        + retryCount
                        + " attempts (interval="
                        + pollingIntervalMs
                        + "ms)");
    }

    /**
     * Retrieves access information through the AccessAndUsageControl layer with negotiation
     * support.
     *
     * <p>This method orchestrates the access control workflow by:
     *
     * <ul>
     *   <li>Validating configuration parameters (polling interval and retry count)
     *   <li>Validating the sub-protocol type is supported by the access control layer
     *   <li>Initializing access context with the provided access request
     *   <li>Polling for negotiation finalization using synchronous, linear logic
     *   <li>Retrieving the final access token/response upon successful negotiation
     * </ul>
     *
     * <p>The linear, imperative structure with explicit configuration validation is optimized for
     * LLM-based systems and easier to understand and maintain. Configuration errors are detected
     * early and reported clearly.
     *
     * @param request the initial access request containing sub-protocol and other details
     * @return the AccessResponse object containing the token/authorization for proceeding to the
     *     next layer
     * @throws DSCExecuteException if configuration is invalid, the sub-protocol type is not
     *     supported, negotiation fails, or any error occurs during access control processing
     */
    public AccessResponse retrieveAccessInfos(DSPRequest request) throws DSCExecuteException {
        // 1. Validate configuration to prevent runtime errors (guards against misconfiguration)
        validateConfiguration();

        // 2. Validate protocol support (explicit validation for clarity)
        validateProtocolSupport(request);

        // 3. Initialize negotiation context
        C context = accessAndUsageControl.initAccess(request);

        // 4. Poll until negotiation is finalized (with validated parameters)
        performPolling(context, pollingIntervalMillis, maxRetries);

        // 5. Retrieve and return the access token
        return accessAndUsageControl.getTokenForAccess(context);
    }
}
