/*
 * Copyright (c) 2026 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
