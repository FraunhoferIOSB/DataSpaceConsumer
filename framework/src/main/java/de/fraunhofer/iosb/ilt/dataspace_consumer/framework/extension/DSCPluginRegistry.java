package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.AccessAndUsageControl;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.adapter.Adapter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.config.Configurable;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.converter.Converter;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.discovery.Discovery;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.api.gate.Gate;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCService;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry for managing and caching loaded MX-Port plugins.
 *
 * <p>This component handles the lazy loading, caching, and lifecycle management of plugins for each
 * MX-Port. Plugins are loaded once per MX-Port and cached to avoid redundant loading and
 * configuration injection. Configuration is injected during the initial load.
 *
 * <p>If any required plugin is not found or fails to load, a {@link DSCExecuteException} is thrown
 * immediately (Fail-Fast pattern).
 */
@Component
public class DSCPluginRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSCPluginRegistry.class);

    /** Service for retrieving MX-Port configurations */
    private final DSCService mxPortService;

    /** Service for managing and retrieving loaded extensions/plugins */
    private final ExtensionService extensionService;

    /** Cache mapping MX-Port names to their loaded plugins */
    private final Map<String, LoadedPlugins> pluginCache = new HashMap<>();

    /**
     * Constructs the MXPortPluginRegistry with required services.
     *
     * @param mxPortService the MXPortService for accessing MX-Port configurations
     * @param extensionService the ExtensionService for loading plugin implementations
     */
    public DSCPluginRegistry(DSCService mxPortService, ExtensionService extensionService) {
        this.mxPortService = mxPortService;
        this.extensionService = extensionService;
    }

    /**
     * Initializes the registry by eagerly loading and validating all plugins for all configured
     * MX-Ports.
     *
     * <p>This method is called automatically by Spring after bean construction. It ensures that all
     * MX-Port plugins are available and properly configured at application startup. If any plugin
     * is missing or configuration fails, a RuntimeException is thrown, preventing the application
     * from starting.
     *
     * @throws RuntimeException if any MX-Port plugin cannot be loaded or configured
     */
    @PostConstruct
    public void initializeAllPorts() {
        // Initialize the plugin system and load all plugins from the extensions directory
        extensionService.initializePlugins();

        List<DSCConfig> allPorts = mxPortService.getMxPorts();

        if (allPorts.isEmpty()) {
            LOGGER.info("No MX-Ports configured");
            return;
        }

        LOGGER.info("Initializing plugins for {} configured MX-Port(s)", allPorts.size());

        for (DSCConfig portConfig : allPorts) {
            String portName = portConfig.getName();
            try {
                LOGGER.info("Loading and validating plugins for MX-Port: {}", portName);
                getPluginsForPort(portName);
                LOGGER.info("Successfully loaded plugins for MX-Port: {}", portName);
            } catch (DSCExecuteException e) {
                LOGGER.error(
                        "Failed to initialize MX-Port: {} - Application startup aborted",
                        portName,
                        e);
                throw new RuntimeException(
                        "Failed to initialize MX-Port '" + portName + "': " + e.getMessage(), e);
            }
        }

        LOGGER.info("All MX-Ports initialized successfully");
    }

    /**
     * Retrieves the loaded plugins for a specific MX-Port.
     *
     * <p>If the plugins are not yet cached for this MX-Port, they are loaded and configured lazily
     * on first access. Subsequent accesses return the cached plugins.
     *
     * @param mxPortName the name of the MX-Port to retrieve plugins for
     * @return the LoadedPlugins containing all configured plugin instances
     * @throws DSCExecuteException if the MX-Port configuration is not found, required plugins are
     *     missing, or configuration injection fails
     */
    public LoadedPlugins getPluginsForPort(String mxPortName) throws DSCExecuteException {
        if (pluginCache.containsKey(mxPortName)) {
            LOGGER.debug("Returning cached plugins for MX-Port: {}", mxPortName);
            return pluginCache.get(mxPortName);
        }

        LOGGER.debug("Loading and caching plugins for MX-Port: {}", mxPortName);
        LoadedPlugins plugins = loadAndCachePlugins(mxPortName);
        pluginCache.put(mxPortName, plugins);
        return plugins;
    }

    /**
     * Loads all required plugins for a specific MX-Port and injects their configurations.
     *
     * <p>This method performs the following steps:
     *
     * <ol>
     *   <li>Retrieves the MX-Port configuration
     *   <li>Loads each plugin (Discovery, AccessAndUsageControl, Gate, Converter, Adapter)
     *   <li>Injects configuration into each plugin if they implement Configurable
     *   <li>Validates that all required plugins are present
     *   <li>Returns a LoadedPlugins object containing all plugins
     * </ol>
     *
     * @param mxPortName the name of the MX-Port to load plugins for
     * @return the LoadedPlugins containing all configured plugin instances
     * @throws DSCExecuteException if the MX-Port configuration is not found, required plugins are
     *     missing, or configuration injection fails
     */
    private LoadedPlugins loadAndCachePlugins(String mxPortName) throws DSCExecuteException {
        // Retrieve the MX-Port configuration
        DSCConfig portConfig = mxPortService.getPortByName(mxPortName);
        if (portConfig == null) {
            LOGGER.error("MX-Port configuration not found: {}", mxPortName);
            throw new DSCExecuteException("MX-Port configuration not found: " + mxPortName);
        }

        // Load all plugins
        @SuppressWarnings("rawtypes")
        Discovery discovery = loadDiscoveryPlugin(portConfig);
        AccessAndUsageControl accessControl = loadAccessAndUsageControlPlugin(portConfig);
        Gate gate = loadGatePlugin(portConfig);
        Converter converter = loadConverterPlugin(portConfig);
        Adapter adapter = loadAdapterPlugin(portConfig);

        // Validate that all required plugins are present
        if (accessControl == null || gate == null || converter == null || adapter == null) {
            LOGGER.error("Failed to load all required plugins for MX-Port: {}", mxPortName);
            throw new DSCExecuteException(
                    "Failed to load all required plugins for MX-Port: " + mxPortName);
        }

        // Inject configurations into plugins
        LOGGER.debug("Injecting configurations for MX-Port plugins: {}", mxPortName);
        injectConfiguration(discovery, portConfig.getDiscovery());
        injectConfiguration(accessControl, portConfig.getAccessAndUsageControl());
        injectConfiguration(gate, portConfig.getGate());
        injectConfiguration(converter, portConfig.getConverter());
        injectConfiguration(adapter, portConfig.getAdapter());

        LOGGER.debug("All plugins loaded and configured for MX-Port: {}", mxPortName);
        return new LoadedPlugins(discovery, accessControl, gate, converter, adapter);
    }

    /**
     * Loads the Discovery plugin implementation for the specified MX-Port configuration.
     *
     * @param portConfig the MX-Port configuration containing discovery component information
     * @return the loaded Discovery implementation, or null if not configured or not found
     */
    @SuppressWarnings("rawtypes")
    private Discovery loadDiscoveryPlugin(DSCConfig portConfig) {
        return loadPluginByComponentConfig(portConfig.getDiscovery(), Discovery.class, "Discovery");
    }

    /**
     * Loads the AccessAndUsageControl plugin implementation for the specified MX-Port
     * configuration.
     *
     * @param portConfig the MX-Port configuration containing implementation class information
     * @return the loaded AccessAndUsageControl implementation, or null if not found
     */
    private AccessAndUsageControl loadAccessAndUsageControlPlugin(DSCConfig portConfig) {
        return loadPluginByComponentConfig(
                portConfig.getAccessAndUsageControl(),
                AccessAndUsageControl.class,
                "AccessAndUsageControl");
    }

    /**
     * Loads the Gate plugin implementation for the specified MX-Port configuration.
     *
     * @param portConfig the MX-Port configuration containing gate class information
     * @return the loaded Gate implementation, or null if not found
     */
    private Gate loadGatePlugin(DSCConfig portConfig) {
        return loadPluginByComponentConfig(portConfig.getGate(), Gate.class, "Gate");
    }

    /**
     * Loads the Converter plugin implementation for the specified MX-Port configuration.
     *
     * @param portConfig the MX-Port configuration containing converter class information
     * @return the loaded Converter implementation, or null if not found
     */
    private Converter loadConverterPlugin(DSCConfig portConfig) {
        return loadPluginByComponentConfig(portConfig.getConverter(), Converter.class, "Converter");
    }

    /**
     * Loads the Adapter plugin implementation for the specified MX-Port configuration.
     *
     * @param portConfig the MX-Port configuration containing adapter class information
     * @return the loaded Adapter implementation, or null if not found
     */
    private Adapter loadAdapterPlugin(DSCConfig portConfig) {
        return loadPluginByComponentConfig(portConfig.getAdapter(), Adapter.class, "Adapter");
    }

    /**
     * Loads a plugin implementation based on the component configuration.
     *
     * <p>This helper method retrieves the implementation class name from the component
     * configuration, searches for a matching loaded plugin, and returns the instance.
     *
     * @param <T> the type of plugin to load
     * @param componentConfig the component configuration containing the implementation class name
     * @param pluginInterface the interface/class type of the plugin
     * @param componentName the name of the component for logging purposes
     * @return the loaded plugin implementation, or null if not found
     */
    private <T> T loadPluginByComponentConfig(
            de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCComponentConfig
                    componentConfig,
            Class<T> pluginInterface,
            String componentName) {

        if (componentConfig == null) {
            LOGGER.warn("Component configuration is null for: {}", componentName);
            return null;
        }

        String implementationClassName = componentConfig.getImplementation();
        if (implementationClassName == null || implementationClassName.isEmpty()) {
            LOGGER.warn("No implementation class specified for component: {}", componentName);
            return null;
        }

        return findPluginByClassName(implementationClassName, pluginInterface, componentName);
    }

    /**
     * Searches for a loaded plugin by its fully qualified class name.
     *
     * <p>Retrieves all loaded plugins of the specified type and finds the one matching the provided
     * class name.
     *
     * @param <T> the type of plugin to search for
     * @param className the fully qualified class name of the plugin implementation
     * @param pluginInterface the interface/class type of the plugin
     * @param componentName the name of the component for logging purposes
     * @return the matching plugin implementation, or null if not found
     */
    private <T> T findPluginByClassName(
            String className, Class<T> pluginInterface, String componentName) {

        LOGGER.debug("Searching for plugin implementation: {} ({})", className, componentName);

        List<T> plugins = extensionService.getPluginsByType(pluginInterface);

        T plugin =
                plugins.stream()
                        .filter(p -> p.getClass().getName().equals(className))
                        .findFirst()
                        .orElse(null);

        if (plugin == null) {
            LOGGER.warn(
                    "Plugin implementation not found: {} for component: {}",
                    className,
                    componentName);
        } else {
            LOGGER.debug(
                    "Plugin implementation loaded successfully: {} for component: {}",
                    className,
                    componentName);
        }

        return plugin;
    }

    /**
     * Injects configuration into a plugin if it implements the Configurable interface.
     *
     * <p>This method checks if the plugin instance implements Configurable and, if so, calls
     * setConfiguration with the config from the component configuration.
     *
     * @param plugin the plugin instance that may be configurable
     * @param componentConfig the component configuration containing the config to inject
     * @throws DSCExecuteException if configuration injection fails
     */
    private void injectConfiguration(
            Object plugin,
            de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCComponentConfig
                    componentConfig)
            throws DSCExecuteException {
        if (plugin == null || componentConfig == null) {
            return;
        }

        // Check if plugin implements Configurable interface and cast in one step
        if (plugin instanceof Configurable configurable) {
            LOGGER.debug("Injecting configuration into plugin: {}", plugin.getClass().getName());
            try {
                configurable.setConfiguration(componentConfig.getConfig());
            } catch (IllegalArgumentException e) {
                LOGGER.error(
                        "Failed to inject configuration for plugin: {}",
                        plugin.getClass().getName(),
                        e);
                throw new DSCExecuteException("Configuration injection failed", e);
            }

            LOGGER.debug(
                    "Configuration injected successfully for plugin: {}",
                    plugin.getClass().getName());
        }
    }

    /**
     * Inner class containing all loaded plugins for a specific MX-Port.
     *
     * <p>This immutable container holds references to all five configured plugin components. All
     * plugins are guaranteed to be non-null, discovery may be null if optional.
     */
    public static final class LoadedPlugins {
        @SuppressWarnings("rawtypes")
        private final Discovery discovery;

        private final AccessAndUsageControl accessAndUsageControl;
        private final Gate gate;
        private final Converter converter;
        private final Adapter adapter;

        /**
         * Constructs a LoadedPlugins container with all plugin instances.
         *
         * @param discovery the Discovery plugin (may be null if optional)
         * @param accessAndUsageControl the AccessAndUsageControl plugin (non-null)
         * @param gate the Gate plugin (non-null)
         * @param converter the Converter plugin (non-null)
         * @param adapter the Adapter plugin (non-null)
         */
        public LoadedPlugins(
                @SuppressWarnings("rawtypes") Discovery discovery,
                AccessAndUsageControl accessAndUsageControl,
                Gate gate,
                Converter converter,
                Adapter adapter) {
            this.discovery = discovery;
            this.accessAndUsageControl = accessAndUsageControl;
            this.gate = gate;
            this.converter = converter;
            this.adapter = adapter;
        }

        @SuppressWarnings("rawtypes")
        public Discovery getDiscovery() {
            return discovery;
        }

        public AccessAndUsageControl getAccessAndUsageControl() {
            return accessAndUsageControl;
        }

        public Gate getGate() {
            return gate;
        }

        public Converter getConverter() {
            return converter;
        }

        public Adapter getAdapter() {
            return adapter;
        }
    }
}
