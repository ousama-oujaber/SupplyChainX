package com.protocol.supplychainx.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        try {
            // if it work
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("status", 200);
            response.put("error", null);
            response.put("message", "Application is healthy");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("status", 500);
            response.put("error", "Internal Server Error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health/details")
    public ResponseEntity<Map<String, Object>> healthDetails() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        // Application info
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("application", "SupplyChainX");

        // Database check
        Map<String, Object> dbHealth = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            if (connection != null && !connection.isClosed()) {
                dbHealth.put("status", "UP");
                dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
                connection.close();
            } else {
                dbHealth.put("status", "DOWN");
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        components.put("db", dbHealth);

        // Disk space check
        Map<String, Object> diskHealth = new HashMap<>();
        try {
            long freeSpace = new java.io.File("/").getFreeSpace();
            long totalSpace = new java.io.File("/").getTotalSpace();
            diskHealth.put("status", "UP");
            diskHealth.put("free", formatBytes(freeSpace));
            diskHealth.put("total", formatBytes(totalSpace));
        } catch (Exception e) {
            log.error("Disk health check failed", e);
            diskHealth.put("status", "DOWN");
            diskHealth.put("error", e.getMessage());
        }
        components.put("diskSpace", diskHealth);

        // Mail health (without actually connecting)
        Map<String, Object> mailHealth = new HashMap<>();
        mailHealth.put("status", "UP");
        mailHealth.put("note", "Mail service available (not tested)");
        components.put("mail", mailHealth);

        response.put("components", components);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "SupplyChainX");
        info.put("description", "Complete Supply Chain Management System API");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Map<String, String> team = new HashMap<>();
        team.put("name", "SupplyChainX Support Team");
        team.put("email", "support@supplychainx.com");
        info.put("contact", team);

        return ResponseEntity.ok(info);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}