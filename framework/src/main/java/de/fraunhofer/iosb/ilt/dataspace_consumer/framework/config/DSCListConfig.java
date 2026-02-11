package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Spring configuration class that binds the list of configured MX-Ports from the application
 * environment into a {@link List} of {@link DSCConfig}.
 *
 * <p>The bean method reads the top-level property {@code mx-port} and returns an empty list when
 * the property is not present.
 */
@Configuration
public class DSCListConfig {
    @Bean
    public List<DSCConfig> mxPorts(Environment environment) {
        Binder binder = Binder.get(environment);
        return binder.bind("mx-port", Bindable.listOf(DSCConfig.class))
                .orElse(Collections.emptyList());
    }
}
