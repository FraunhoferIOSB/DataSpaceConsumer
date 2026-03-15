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
package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.trigger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCExecutor;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.DSCService;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.DSCConfig;
import de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config.TriggerConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTrigger extends Trigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTrigger.class);

    private final DSCService mxPortService;
    private final TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public SchedulerTrigger(
            DSCExecutor mxPortExecutor, DSCService mxPortService, TaskScheduler taskScheduler) {
        super(mxPortExecutor);
        this.mxPortService = mxPortService;
        if (taskScheduler != null) {
            this.taskScheduler = taskScheduler;
        } else {
            // Fallback to a simple ThreadPoolTaskScheduler when no TaskScheduler bean is available
            ThreadPoolTaskScheduler fallback = new ThreadPoolTaskScheduler();
            fallback.setPoolSize(1);
            fallback.setThreadNamePrefix("mxport-scheduler-");
            fallback.initialize();
            this.taskScheduler = fallback;
        }
    }

    /**
     * * Initialize all schedulers on application startup. This method is automatically called after
     * bean creation.
     */
    @PostConstruct
    public void init() {
        LOGGER.info(
                "SchedulerTrigger: Initializing scheduled tasks for all configured MX-Ports...");

        for (DSCConfig config : mxPortService.getMxPorts()) {
            try {
                schedule(config.getName());
            } catch (Exception e) {
                LOGGER.error(
                        "SchedulerTrigger: Failed to schedule MX-Port '{}': {}",
                        config.getName(),
                        e.getMessage(),
                        e);
            }
        }

        LOGGER.info(
                "SchedulerTrigger: Initialization complete. {} task(s) scheduled.",
                scheduledTasks.size());
    }

    /**
     * Register a cron-based schedule for the given MX-Port name if enabled in the port config. If a
     * schedule already exists for the port it will be left unchanged.
     *
     * <p>The cron expression is taken from the MX-Port's {@link TriggerConfig}. When scheduling is
     * disabled or the MX-Port does not exist, any previously registered task for the port will be
     * cancelled.
     *
     * @param mxPortName the name of the MX-Port to schedule
     */
    public void schedule(String mxPortName) {
        DSCConfig portConfig = mxPortService.getPortByName(mxPortName);
        if (portConfig == null) {
            LOGGER.warn(
                    "SchedulerTrigger: MX-Port '{}' not found. Aborting scheduling.", mxPortName);
            return;
        }

        TriggerConfig trigger = portConfig.getTrigger();
        if (trigger == null
                || trigger.getScheduler() == null
                || !Boolean.TRUE.equals(trigger.getScheduler().getEnabled())) {
            LOGGER.info("SchedulerTrigger: Scheduler is not enabled for MX-Port '{}'.", mxPortName);
            // If there is a previously registered task, cancel it
            cancelIfScheduled(mxPortName);
            return;
        }

        String cron = trigger.getScheduler().getCron();
        if (cron == null || cron.isBlank()) {
            LOGGER.warn(
                    "SchedulerTrigger: No cron expression configured for MX-Port '{}'. Aborting"
                            + " scheduling.",
                    mxPortName);
            return;
        }

        // Prevent double-scheduling
        if (scheduledTasks.containsKey(mxPortName)) {
            LOGGER.info(
                    "SchedulerTrigger: Scheduler already registered for MX-Port '{}'. Skipping.",
                    mxPortName);
            return;
        }

        Runnable task =
                () -> {
                    try {
                        LOGGER.debug(
                                "SchedulerTrigger: Triggering MX-Port '{}' via scheduler.",
                                mxPortName);
                        execute(mxPortName, portConfig.getTimeout());
                    } catch (Exception e) {
                        LOGGER.error(
                                "SchedulerTrigger: Error while executing MX-Port '{}'",
                                mxPortName,
                                e);
                    }
                };

        ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cron));
        if (future != null) {
            scheduledTasks.put(mxPortName, future);
            LOGGER.info(
                    "SchedulerTrigger: Scheduler registered for MX-Port '{}' (cron='{}').",
                    mxPortName,
                    cron);
        } else {
            LOGGER.warn(
                    "SchedulerTrigger: TaskScheduler returned null when scheduling MX-Port '{}'.",
                    mxPortName);
        }
    }

    private void cancelIfScheduled(String mxPortName) {
        ScheduledFuture<?> existing = scheduledTasks.remove(mxPortName);
        if (existing != null) {
            existing.cancel(false);
            LOGGER.info(
                    "SchedulerTrigger: Previous scheduler for MX-Port '{}' was cancelled.",
                    mxPortName);
        }
    }
}
