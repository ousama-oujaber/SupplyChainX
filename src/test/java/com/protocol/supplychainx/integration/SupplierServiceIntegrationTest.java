package com.protocol.supplychainx.integration;

import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import com.protocol.supplychainx.procurement.service.ISupplierService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SupplierServiceIntegrationTest {

    @Autowired
    private ISupplierService supplierService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Integration: Should create and retrieve supplier successfully")
    void testCreateAndRetrieveSupplier() {
        // Arrange
        SupplierDTO supplierDTO = SupplierDTO.builder()
                .name("Integration Supplier")
                .contact("integration@supplier.com")
                .rating(4.8)
                .leadTime(5)
                .build();

        // Act
        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);
        SupplierDTO retrievedSupplier = supplierService.getSupplier(createdSupplier.getIdSupplier());

        // Assert
        assertNotNull(createdSupplier.getIdSupplier());
        assertEquals("Integration Supplier", retrievedSupplier.getName());
        assertEquals(4.8, retrievedSupplier.getRating());
    }

    @Test
    @DisplayName("Integration: Should update supplier successfully")
    void testUpdateSupplier() {
        // Arrange
        SupplierDTO supplierDTO = SupplierDTO.builder()
                .name("Update Test Supplier")
                .contact("update@supplier.com")
                .rating(4.0)
                .leadTime(7)
                .build();

        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);

        // Act
        createdSupplier.setName("Updated Supplier Name");
        createdSupplier.setRating(5.0);
        SupplierDTO updatedSupplier = supplierService.updateSupplier(createdSupplier.getIdSupplier(), createdSupplier);

        // Assert
        assertEquals("Updated Supplier Name", updatedSupplier.getName());
        assertEquals(5.0, updatedSupplier.getRating());
    }

    @Test
    @DisplayName("Integration: Should search suppliers by name")
    void testSearchSuppliersByName() {
        // Arrange
        SupplierDTO supplier1 = SupplierDTO.builder()
                .name("SearchTest Supplier 1")
                .contact("search1@supplier.com")
                .rating(4.5)
                .leadTime(5)
                .build();

        SupplierDTO supplier2 = SupplierDTO.builder()
                .name("SearchTest Supplier 2")
                .contact("search2@supplier.com")
                .rating(4.2)
                .leadTime(6)
                .build();

        supplierService.createSupplier(supplier1);
        supplierService.createSupplier(supplier2);

        // Act
        Page<SupplierDTO> results = supplierService.searchSuppliersByName("SearchTest", PageRequest.of(0, 10));

        // Assert
        assertNotNull(results);
        assertTrue(results.getContent().size() >= 2);
    }

    @Test
    @DisplayName("Integration: Should delete supplier successfully")
    void testDeleteSupplier() {
        // Arrange
        SupplierDTO supplierDTO = SupplierDTO.builder()
                .name("Delete Test Supplier")
                .contact("delete@supplier.com")
                .rating(4.0)
                .leadTime(5)
                .build();

        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);

        // Act
        supplierService.deleteSupplier(createdSupplier.getIdSupplier());

        // Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            supplierService.getSupplier(createdSupplier.getIdSupplier())
        );
    }

    @Test
    @DisplayName("Integration: Should throw SupplierNotFoundException for non-existent supplier")
    void testGetNonExistentSupplier() {
        // Act & Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            supplierService.getSupplier(99999L)
        );
    }
}

