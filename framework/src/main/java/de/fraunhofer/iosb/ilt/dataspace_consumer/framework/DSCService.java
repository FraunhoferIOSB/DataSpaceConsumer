package de.fraunhofer.iosb.ilt.dataspace_consumer.framework;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import org.springframework.stereotype.Service;

@Service
public class DSCService {
    /** List of configured MX-Port instances injected by Spring. */
    private final List<DSCConfig> mxPorts;

    public DSCService(List<DSCConfig> mxPorts) {
        this.mxPorts = mxPorts;
    }

    /**
     * Returns all configured MX-Ports.
     *
     * @return list of MXPortConfig
     */
    public List<DSCConfig> getMxPorts() {
        return mxPorts;
    }

    /**
     * Find a configured MX-Port by name.
     *
     * @param name the MX-Port name to search for
     * @return the MXPortConfig or {@code null} if not found
     */
    public DSCConfig getPortByName(String name) {
        return mxPorts.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }
}
