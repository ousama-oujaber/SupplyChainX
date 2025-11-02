package com.protocol.supplychainx.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Configuration for Spring Scheduler
 * Configures a thread pool for scheduled tasks to run concurrently
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * Configure task scheduler with a thread pool
     * This allows multiple scheduled tasks to run concurrently without blocking each other
     *
     * @param taskRegistrar The task registrar to configure
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        
        // Configure pool size (number of concurrent scheduled tasks)
        taskScheduler.setPoolSize(5);
        
        // Set thread name prefix for easier debugging
        taskScheduler.setThreadNamePrefix("scheduled-task-");
        
        // Set thread name prefix for pool threads
        taskScheduler.setThreadNamePrefix("scheduler-");
        
        // Wait for scheduled tasks to complete before shutdown
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // Maximum time to wait for tasks to complete during shutdown (in seconds)
        taskScheduler.setAwaitTerminationSeconds(60);
        
        // Initialize the scheduler
        taskScheduler.initialize();
        
        // Register the scheduler
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
