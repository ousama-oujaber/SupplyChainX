package com.protocol.supplychainx;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        // Use MySQL 8.0.33 for compatibility and stability
        // MySQL 9.x has deprecated innodb_log_file_size and other breaking changes
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);
    }

}
