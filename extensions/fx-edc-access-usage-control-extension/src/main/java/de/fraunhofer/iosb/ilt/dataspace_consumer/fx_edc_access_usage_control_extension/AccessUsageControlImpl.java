package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessResponse;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.SubProtocolType;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPAccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp.DSPRequest;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.EdcClient;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.AvailableEdrDTO;
import de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc.dto.EdrDTO;
import org.pf4j.Extension;

@Extension
/**
 * Implementation of DSP-based access and usage control that interacts with a FactoryX / EDC-style
 * policy and negotiation API.
 *
 * <p>This class implements the DSPAccessAndUsageControl interface for AuthorizationContext objects
 * and the Configurable interface. It performs catalog queries, policy extraction, negotiation
 * initiation and token retrieval by calling the configured EDC endpoints using OkHttp and parsing
 * JSON responses with Jackson.
 */
public class AccessUsageControlImpl
        implements DSPAccessAndUsageControl<AuthorizationContext>, Configurable {

    private static final Logger LOGGER = Logger.getLogger(AccessUsageControlImpl.class.getName());

    private Caching cache = Caching.getInstance();

    private EdcClient client;

    public AccessUsageControlImpl() {}

    /**
     * Returns a list with supported subprotocol types.
     *
     * @return a list of supported SubProtocolType values. For this implementation the list contains
     *     SubProtocolType.DSP only.
     */
    @Override
    public List<SubProtocolType> getSupportedSubProtocolTypes() {

        List<SubProtocolType> types = new ArrayList<>();
        types.add(SubProtocolType.DSP);
        return types;
    }

    /**
     * Initiate access negotiation for the provided DSP access request.
     *
     * <p>The method queries the catalog for a matching policy, starts a negotiation and returns an
     * AuthorizationContext containing the negotiation id and the asset id. Any failure during the
     * process throws an DSCExecuteException.
     *
     * @param accessRequest the DSPRequest representing the desired access filters
     * @return an AuthorizationContext containing negotiation id and asset id
     * @throws DSCExecuteException if policy lookup or negotiation fails
     */
    @Override
    public AuthorizationContext initAccess(DSPRequest accessRequest) throws DSCExecuteException {

        AuthorizationContext cached = cache.getNegotiation(accessRequest);
        if (cached != null) {
            return cached;
        }
        InitData initData = client.getPolicyFromCatalog(accessRequest);
        String negotiationId = client.initiateNegotiation(initData);
        LOGGER.log(Level.FINE, "asset id: {0}", initData.assetId());
        LOGGER.log(Level.FINE, "negotiation id: {0}", negotiationId);
        AuthorizationContext context = new AuthorizationContext(negotiationId, initData.assetId());
        cache.putNegotiation(accessRequest, context);
        return context;
    }

    /**
     * Check whether the negotiation associated with the given context has reached a finalized
     * state.
     *
     * <p>The method requests the negotiation state from the remote service and returns true when
     * the state equals "FINALIZED". If the negotiation id within the context is null the method
     * returns false.
     *
     * @param context the AuthorizationContext containing the negotiation id
     * @return true if the negotiation state is "FINALIZED", false otherwise
     */
    @Override
    public boolean isNegotiationFinalized(AuthorizationContext context) {

        return client.isNegotiationFinalized(context);
    }

    /**
     * Obtain an AccessResponse (endpoint + token) for the given AuthorizationContext.
     *
     * <p>The method looks up available EDRs for the context's asset id, selects the most recent
     * transfer process, fetches the token/data address and returns an AccessResponse containing the
     * endpoint and authorization token. If no available EDRs are found an DSCExecuteException is
     * thrown.
     *
     * @param context the AuthorizationContext containing the asset id
     * @return an AccessResponse with endpoint and token
     * @throws DSCExecuteException if no EDRs are available or token retrieval fails
     */
    @Override
    public AccessResponse getTokenForAccess(AuthorizationContext context)
            throws DSCExecuteException {

        EdrDTO cached = cache.getToken(context);
        if (cached != null) {
            return new AccessResponse(cached.endpoint(), cached.authorization(), context.assetId());
        }

        AvailableEdrDTO edr;
        try {
            edr = client.getAvailableEDRResponse(context.assetId()).getFirst();
        } catch (NoSuchElementException exception) {
            throw new DSCExecuteException("No available EDRs found. No token available");
        }
        String newestTransferProcessID = edr.transferProcessId();
        EdrDTO dto = client.getEDRTokenResponse(newestTransferProcessID);
        cache.putToken(context, dto);
        LOGGER.log(Level.FINE, "endpoint: {0}", dto.endpoint());
        LOGGER.log(Level.FINE, "token: {0}", LoggingUtil.maskToken(dto.authorization(), 4));

        return new AccessResponse(dto.endpoint(), dto.authorization(), context.assetId());
    }

    /**
     * Configure the EDC client parameters used by this implementation.
     *
     * <p>The provided configuration map must contain the keys "baseUrl", "apiKey",
     * "counterPartyAddress" and "counterPartyId". Passing a null configuration will result in an
     * IllegalArgumentException.
     *
     * @param config configuration map containing required entries
     * @throws IllegalArgumentException if config is null
     */
    @Override
    public void setConfiguration(Map<String, Object> config) throws IllegalArgumentException {

        if (config == null) {
            throw new IllegalArgumentException("Configuration must not be null");
        }

        String baseURL = config.get("baseUrl").toString();
        String apiKey = config.get("apiKey").toString();
        String counterPartyAddress = config.get("counterPartyAddress").toString();
        String counterPartyId = config.get("counterPartyId").toString();

        client = new EdcClient(baseURL, apiKey, counterPartyId, counterPartyAddress);
    }
}
