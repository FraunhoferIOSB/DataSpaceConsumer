package de.fraunhofer.iosb.ilt.dataspace_consumer.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class that provides scheduling-related beans for the MX-Port Consumer Framework.
 *
 * <p>This configuration defines the {@link TaskScheduler} bean used by the framework for scheduling
 * MX-Port executions.
 */
@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    /**
     * Creates and configures a {@link ThreadPoolTaskScheduler} bean.
     *
     * @return configured TaskScheduler instance
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("mxport-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
