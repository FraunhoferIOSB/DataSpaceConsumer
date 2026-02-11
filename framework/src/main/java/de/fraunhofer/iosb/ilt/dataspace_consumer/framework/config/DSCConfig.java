package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

/**
 * Configuration for a single MX-Port instance.
 *
 * <p>This POJO is used to bind the {@code mx-port} entries from the application configuration (e.g.
 * application.yaml). It contains references to component configurations for each layer (access
 * control, gate, converter and adapter) as well as optional negotiation and trigger settings.
 */
public class DSCConfig {
    private String name;
    private DSCComponentConfig discovery;
    private DSCComponentConfig accessAndUsageControl;
    private DSCComponentConfig gate;
    private DSCComponentConfig converter;
    private DSCComponentConfig adapter;
    private Long negotiationTimeoutMillis;
    private Integer negotiationMaxRetries;
    private TriggerConfig trigger;

    /**
     * Returns the configured name of this MX-Port.
     *
     * @return the MX-Port name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the configuration for the Discovery component.
     *
     * @return discovery component config
     */
    public DSCComponentConfig getDiscovery() {
        return discovery;
    }

    /**
     * Returns the configuration for the Access and Usage Control component.
     *
     * @return access control component config
     */
    public DSCComponentConfig getAccessAndUsageControl() {
        return accessAndUsageControl;
    }

    /**
     * Returns the configuration for the Gate component.
     *
     * @return gate component config
     */
    public DSCComponentConfig getGate() {
        return gate;
    }

    /**
     * Returns the configuration for the Converter component.
     *
     * @return converter component config
     */
    public DSCComponentConfig getConverter() {
        return converter;
    }

    /**
     * Returns the configuration for the Adapter component.
     *
     * @return adapter component config
     */
    public DSCComponentConfig getAdapter() {
        return adapter;
    }

    /**
     * Returns the negotiation timeout in milliseconds used when waiting for access negotiation to
     * finish. May be {@code null} to indicate a default should be used by the executor.
     *
     * @return timeout in milliseconds or {@code null}
     */
    public Long getNegotiationTimeoutMillis() {
        return negotiationTimeoutMillis;
    }

    /**
     * Returns the maximum number of polling attempts when waiting for negotiation finalization. May
     * be {@code null} to indicate a default.
     *
     * @return maximum retries or {@code null}
     */
    public Integer getNegotiationMaxRetries() {
        return negotiationMaxRetries;
    }

    /**
     * Returns the optional trigger configuration for this MX-Port (resthook, scheduler, ...).
     *
     * @return trigger configuration or {@code null}
     */
    public TriggerConfig getTrigger() {
        return trigger;
    }

    public void setTrigger(TriggerConfig trigger) {
        this.trigger = trigger;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDiscovery(DSCComponentConfig discovery) {
        this.discovery = discovery;
    }

    public void setAccessAndUsageControl(DSCComponentConfig accessAndUsageControl) {
        this.accessAndUsageControl = accessAndUsageControl;
    }

    public void setGate(DSCComponentConfig gate) {
        this.gate = gate;
    }

    public void setConverter(DSCComponentConfig converter) {
        this.converter = converter;
    }

    public void setAdapter(DSCComponentConfig adapter) {
        this.adapter = adapter;
    }

    public void setNegotiationMaxRetries(Integer negotiationMaxRetries) {
        this.negotiationMaxRetries = negotiationMaxRetries;
    }

    public void setNegotiationTimeoutMillis(Long negotiationTimeoutMillis) {
        this.negotiationTimeoutMillis = negotiationTimeoutMillis;
    }
}
