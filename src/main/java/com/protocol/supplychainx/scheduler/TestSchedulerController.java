package com.protocol.supplychainx.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/scheduler")
@RequiredArgsConstructor
@Slf4j
public class TestSchedulerController {
    
    private final LowStockAlertScheduler scheduler;

    @GetMapping("/trigger-low-stock-check")
    public ResponseEntity<Map<String, String>> triggerLowStockCheck() {
        log.info("🎯 Manual trigger received for low stock check");
        
        try {
            scheduler.checkLowStockMaterials();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "✅ Low stock check triggered successfully!");
            response.put("action", "Check application logs and your email inbox");
            response.put("email", "supplysyncx@gmail.com");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error triggering low stock check", e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to trigger low stock check");
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("scheduler", "enabled");
        response.put("email", "supplysyncx@gmail.com");
        response.put("message", "Scheduler is running");
        
        return ResponseEntity.ok(response);
    }
}
