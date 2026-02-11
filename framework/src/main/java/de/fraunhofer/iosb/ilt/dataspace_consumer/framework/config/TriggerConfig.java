package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

/**
 * Configuration holder for trigger settings of an MX-Port.
 *
 * <p>Supports two trigger types:
 *
 * <ul>
 *   <li>Rest hook — configuration under {@code restHook}
 *   <li>Scheduler — configuration under {@code scheduler}
 * </ul>
 */
public class TriggerConfig {
    private RestHookConfig restHook;
    private SchedulerConfig scheduler;

    /**
     * Returns the rest hook configuration.
     *
     * @return rest hook config or {@code null}
     */
    public RestHookConfig getRestHook() {
        return restHook;
    }

    public void setRestHook(RestHookConfig restHook) {
        this.restHook = restHook;
    }

    /**
     * Returns the scheduler configuration.
     *
     * @return scheduler config or {@code null}
     */
    public SchedulerConfig getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerConfig scheduler) {
        this.scheduler = scheduler;
    }

    /** Rest hook specific settings. */
    public static class RestHookConfig {
        private Boolean enabled;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** Scheduler specific settings such as cron expression and enable flag. */
    public static class SchedulerConfig {
        private Boolean enabled;
        private String cron;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }
    }
}
