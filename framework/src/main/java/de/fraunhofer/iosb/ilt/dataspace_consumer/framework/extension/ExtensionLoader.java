package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.extension;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

/**
 * Loads and manages extensions (plugins) from the extensions directory.
 *
 * <p>This component is responsible for initializing and managing the PF4J ({@code Plugin Framework
 * for Java}) {@link PluginManager}. It configures the plugin manager to use the {@code extensions}
 * directory and provides lifecycle operations such as load/start and stop/unload.
 */
@Component
public class ExtensionLoader {

    /** Path to the extensions directory */
    private static final String EXTENSIONS_DIR = "extensions";

    /** The PF4J plugin manager instance */
    private final PluginManager pluginManager;

    /**
     * Initializes the ExtensionLoader with a {@link DefaultPluginManager} that uses the configured
     * {@code extensions} directory.
     */
    public ExtensionLoader() {
        Path pluginsPath = Paths.get(EXTENSIONS_DIR);
        this.pluginManager = new DefaultPluginManager(pluginsPath);
    }

    /**
     * Loads and starts all plugins discovered in the extensions directory.
     *
     * <p>Discovery will scan for plugin JARs, load them into the plugin manager and call {@code
     * start()} on the loaded plugins. Plugins are started respecting declared dependencies.
     */
    public void loadPlugins() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }

    /**
     * Stops and unloads all currently running plugins.
     *
     * <p>Plugins will be stopped and then unloaded from the plugin manager. This should be invoked
     * during application shutdown to free resources and ensure a clean shutdown of plugin code.
     */
    public void unloadPlugins() {
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
    }

    /**
     * Returns the underlying PF4J {@link PluginManager} instance.
     *
     * @return the PluginManager managing all loaded extensions
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
