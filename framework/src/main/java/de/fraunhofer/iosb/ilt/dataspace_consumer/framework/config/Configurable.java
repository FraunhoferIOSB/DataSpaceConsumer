package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

import java.util.Map;

import org.pf4j.ExtensionPoint;

/**
 * Interface for configurable extensions (plugins).
 *
 * <p>Plugins can optionally implement this interface to receive configuration from the MX-Port
 * configuration file (application.yaml). This allows each plugin instance to have its own custom
 * configuration.
 *
 * <p>Example in application.yaml:
 *
 * <pre>
 * mx-port:
 *   - name: MX-Port1
 *     adapter:
 *       implementation: com.example.MyAdapter
 *       config:
 *         setting1: value1
 *         setting2: value2
 * </pre>
 *
 * The MyAdapter plugin would then implement Configurable to receive this config.
 */
public interface Configurable extends ExtensionPoint {

    /**
     * Sets the configuration for this extension.
     *
     * <p>Called by the framework before the extension is used. The configuration comes from the
     * application.yaml file for the specific MX-Port instance.
     *
     * @param config a Map containing the configuration parameters, or null if no configuration was
     *     specified
     * @throws IllegalArgumentException if the configuration is invalid
     */
    void setConfiguration(Map<String, Object> config) throws IllegalArgumentException;
}
