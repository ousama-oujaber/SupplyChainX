package com.protocol.supplychainx.scheduler;

import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.scheduler.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LowStockAlertSchedulerTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LowStockAlertScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "emailTo", "alerts@supplychainx.com");
    }

    @Test
    @DisplayName("Should do nothing when no materials are below minimum stock")
    void testCheckLowStockMaterials_NoLowStock() {
        when(rawMaterialRepository.findByStockLessThanStockMin()).thenReturn(List.of());

        scheduler.checkLowStockMaterials();

        verify(rawMaterialRepository).findByStockLessThanStockMin();
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendHtmlEmailToMultiple(any(String[].class), anyString(), anyString());
    }

    @Test
    @DisplayName("Should send low stock alert to multiple recipients with detailed report")
    void testCheckLowStockMaterials_SendsToMultipleRecipients() {
        ReflectionTestUtils.setField(scheduler, "emailTo", "team@supply.com,manager@supply.com");
        when(rawMaterialRepository.findByStockLessThanStockMin()).thenReturn(sampleMaterials());

        ArgumentCaptor<String[]> recipientsCaptor = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        scheduler.checkLowStockMaterials();

        verify(emailService).sendHtmlEmailToMultiple(recipientsCaptor.capture(), subjectCaptor.capture(), htmlCaptor.capture());
        assertArrayEquals(new String[]{"team@supply.com", "manager@supply.com"}, recipientsCaptor.getValue());
        assertEquals("⚠️ ALERTE STOCK CRITIQUE - SupplyChainX", subjectCaptor.getValue());
        String html = htmlCaptor.getValue();
        assertNotNull(html);
        assertTrue(html.contains("Matières en Alerte"));
    assertTrue(html.contains("Haute (1)"));
    assertTrue(html.contains("Moyenne (1)"));
    assertTrue(html.contains("Basse (1)"));
    assertTrue(html.contains("Aluminium"));
    assertTrue(html.contains("Copper"));
    assertTrue(html.contains("Plastic"));
    }

    @Test
    @DisplayName("Should fallback to single email and continue when sending fails")
    void testCheckLowStockMaterials_EmailFailureHandled() {
        ReflectionTestUtils.setField(scheduler, "emailTo", "ops@supply.com");
        when(rawMaterialRepository.findByStockLessThanStockMin()).thenReturn(sampleMaterials());
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendHtmlEmail(eq("ops@supply.com"), anyString(), anyString());

        assertDoesNotThrow(() -> scheduler.checkLowStockMaterials());
        verify(emailService).sendHtmlEmail(eq("ops@supply.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should swallow repository failures and log the error")
    void testCheckLowStockMaterials_RepositoryFailureHandled() {
        when(rawMaterialRepository.findByStockLessThanStockMin())
                .thenThrow(new RuntimeException("database offline"));

        assertDoesNotThrow(() -> scheduler.checkLowStockMaterials());
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendHtmlEmailToMultiple(any(String[].class), anyString(), anyString());
    }

    private List<RawMaterial> sampleMaterials() {
        RawMaterial high = RawMaterial.builder()
                .idMaterial(1L)
                .name("Aluminium")
                .stock(40)
                .stockMin(100)
                .unit("kg")
                .build();

        RawMaterial medium = RawMaterial.builder()
                .idMaterial(2L)
                .name("Copper")
                .stock(50)
                .stockMin(80)
                .unit("kg")
                .build();

        RawMaterial low = RawMaterial.builder()
                .idMaterial(3L)
                .name("Plastic")
                .stock(15)
                .stockMin(30)
                .unit("kg")
                .build();

        return List.of(high, medium, low);
    }
}
