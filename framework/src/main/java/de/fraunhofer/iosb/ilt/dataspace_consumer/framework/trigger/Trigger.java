package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.trigger;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Trigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Trigger.class);

    protected final DSCExecutor mxPortExecutor;

    /**
     * Base trigger component that provides a simple execute wrapper around the {@link DSCExecutor}.
     * Concrete trigger implementations (scheduler, rest hook) extend this class.
     */
    @Autowired
    public Trigger(DSCExecutor mxPortExecutor) {
        this.mxPortExecutor = mxPortExecutor;
    }

    /**
     * Execute the named MX-Port using the framework executor. Exceptions thrown by the executor are
     * logged but not rethrown to keep triggers resilient.
     *
     * @param mxPortName the name of the MX-Port to execute
     */
    protected void execute(String mxPortName) {
        try {
            LOGGER.info("Trigger invoked for MX-Port: {}", mxPortName);
            mxPortExecutor.execute(mxPortName);
        } catch (DSCExecuteException e) {
            LOGGER.error("Execution failed for MX-Port {}: {}", mxPortName, e.getMessage(), e);
        }
    }
}
