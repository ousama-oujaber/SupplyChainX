package com.protocol.supplychainx.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestSchedulerControllerTest {

    @Mock
    private LowStockAlertScheduler scheduler;

    @InjectMocks
    private TestSchedulerController controller;

    @Test
    @DisplayName("Should trigger low stock check successfully")
    void testTriggerLowStockCheck_Success() {
        ResponseEntity<Map<String, String>> response = controller.triggerLowStockCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().get("status"));
        assertTrue(response.getBody().get("message").contains("Low stock check"));
        verify(scheduler).checkLowStockMaterials();
    }

    @Test
    @DisplayName("Should return error response when scheduler fails")
    void testTriggerLowStockCheck_Error() {
        doThrow(new RuntimeException("boom")).when(scheduler).checkLowStockMaterials();

        ResponseEntity<Map<String, String>> response = controller.triggerLowStockCheck();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().get("status"));
        assertEquals("Failed to trigger low stock check", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should report scheduler status")
    void testGetStatus() {
        ResponseEntity<Map<String, String>> response = controller.getStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("enabled", response.getBody().get("scheduler"));
        assertEquals("Scheduler is running", response.getBody().get("message"));
    }
}
