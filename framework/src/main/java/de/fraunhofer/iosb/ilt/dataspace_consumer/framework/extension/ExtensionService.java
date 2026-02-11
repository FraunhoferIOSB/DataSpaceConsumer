package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.extension;

import java.util.List;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;

/**
 * Service for managing extensions (plugins) within the MX-Port Consumer Framework.
 *
 * <p>Provides a high-level API for plugin lifecycle management and discovery. This service
 * encapsulates the {@link ExtensionLoader} and exposes convenient methods for initializing,
 * querying and shutting down plugins.
 */
@Service
public class ExtensionService {

    /** The extension loader managing the plugin lifecycle */
    private final ExtensionLoader extensionLoader;

    /**
     * Constructs the ExtensionService with an ExtensionLoader.
     *
     * @param extensionLoader the ExtensionLoader instance to manage plugins
     */
    public ExtensionService(ExtensionLoader extensionLoader) {
        this.extensionLoader = extensionLoader;
    }

    /**
     * Initializes all plugins from the extensions directory.
     *
     * <p>Loads and starts all available plugins. If an error occurs during loading, a
     * RuntimeException is thrown with the cause wrapped.
     *
     * @throws RuntimeException if plugin loading fails
     */
    public void initializePlugins() {
        extensionLoader.loadPlugins();
    }

    /**
     * Returns a list of all loaded plugins.
     *
     * @return a List of PluginWrapper objects for all loaded plugins
     */
    public List<PluginWrapper> getLoadedPlugins() {
        return extensionLoader.getPluginManager().getPlugins();
    }

    /**
     * Returns a list of all started (running) plugins.
     *
     * @return a List of PluginWrapper objects for all started plugins
     */
    public List<PluginWrapper> getStartedPlugins() {
        return extensionLoader.getPluginManager().getStartedPlugins();
    }

    /**
     * Returns all extensions implementing the specified plugin class/interface.
     *
     * @param <T> the type of extensions to retrieve
     * @param pluginClass the Class object representing the plugin interface or type
     * @return a List of extensions implementing the specified class
     */
    public <T> List<T> getPluginsByType(Class<T> pluginClass) {
        return extensionLoader.getPluginManager().getExtensions(pluginClass);
    }

    /**
     * Stops and unloads all running plugins.
     *
     * <p>Performs graceful shutdown of all active plugins. If an error occurs during unloading, a
     * RuntimeException is thrown with the cause wrapped.
     *
     * @throws RuntimeException if plugin unloading fails
     */
    public void shutdownPlugins() {
        extensionLoader.unloadPlugins();
    }
}
