package com.protocol.supplychainx.scheduler.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerConfigTest {

    @Test
    @DisplayName("Should configure task registrar with thread pool scheduler")
    void testConfigureTasks() {
        SchedulerConfig config = new SchedulerConfig();
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();

        config.configureTasks(registrar);

        assertNotNull(registrar.getScheduler());
        assertTrue(registrar.getScheduler() instanceof ThreadPoolTaskScheduler);
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) registrar.getScheduler();
        assertEquals("scheduler-", scheduler.getThreadNamePrefix());
        scheduler.shutdown();
    }
}
