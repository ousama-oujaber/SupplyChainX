package com.protocol.supplychainx.repository;

import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SupplierRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SupplierRepository supplierRepository;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .name("Test Supplier")
                .contact("test@supplier.com")
                .rating(4.5)
                .leadTime(7)
                .build();
    }

    @Test
    @DisplayName("Should save and find supplier by ID")
    void testSaveAndFindById() {
        // Act
        Supplier savedSupplier = entityManager.persistAndFlush(supplier);
        Optional<Supplier> foundSupplier = supplierRepository.findById(savedSupplier.getIdSupplier());

        // Assert
        assertTrue(foundSupplier.isPresent());
        assertEquals("Test Supplier", foundSupplier.get().getName());
        assertEquals(4.5, foundSupplier.get().getRating());
    }

    @Test
    @DisplayName("Should find suppliers by name containing")
    void testFindByNameContainingIgnoreCase() {
        // Arrange
        Supplier supplier2 = Supplier.builder()
                .name("Another Supplier")
                .contact("another@supplier.com")
                .rating(4.0)
                .leadTime(5)
                .build();

        entityManager.persistAndFlush(supplier);
        entityManager.persistAndFlush(supplier2);

        // Act
        Page<Supplier> results = supplierRepository.findByNameContainingIgnoreCase("test", PageRequest.of(0, 10));

        // Assert
        assertNotNull(results);
        assertTrue(results.getContent().size() > 0);
        assertTrue(results.getContent().get(0).getName().toLowerCase().contains("test"));
    }

    @Test
    @DisplayName("Should check if supplier exists")
    void testExistsById() {
        // Arrange
        Supplier savedSupplier = entityManager.persistAndFlush(supplier);

        // Act
        boolean exists = supplierRepository.existsById(savedSupplier.getIdSupplier());
        boolean notExists = supplierRepository.existsById(99999L);

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should delete supplier by ID")
    void testDeleteSupplier() {
        // Arrange
        Supplier savedSupplier = entityManager.persistAndFlush(supplier);
        Long supplierId = savedSupplier.getIdSupplier();

        // Act
        supplierRepository.deleteById(supplierId);
        Optional<Supplier> foundSupplier = supplierRepository.findById(supplierId);

        // Assert
        assertFalse(foundSupplier.isPresent());
    }

    @Test
    @DisplayName("Should count all suppliers")
    void testCount() {
        // Arrange
        Supplier supplier2 = Supplier.builder()
                .name("Another Supplier")
                .contact("another@supplier.com")
                .rating(4.0)
                .leadTime(5)
                .build();

        entityManager.persistAndFlush(supplier);
        entityManager.persistAndFlush(supplier2);

        // Act
        long count = supplierRepository.count();

        // Assert
        assertTrue(count >= 2);
    }
}
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.protocol.supplychainx.procurement.dto.SupplierDTO;
//import com.protocol.supplychainx.procurement.service.ISupplierService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(SupplierController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class SupplierControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private ISupplierService supplierService;
//
//    private SupplierDTO supplierDTO;
//
//    @BeforeEach
//    void setUp() {
//        supplierDTO = SupplierDTO.builder()
//                .idSupplier(1L)
//                .name("Test Supplier")
//                .contact("test@supplier.com")
//                .rating(4.5)
//                .leadTime(7)
//                .build();
//    }
//
//    @Test
//    @DisplayName("Should create supplier successfully")
//    void testCreateSupplier() throws Exception {
//        when(supplierService.createSupplier(any(SupplierDTO.class))).thenReturn(supplierDTO);
//
//        mockMvc.perform(post("/api/suppliers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(supplierDTO)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name").value("Test Supplier"))
//                .andExpect(jsonPath("$.rating").value(4.5));
//
//        verify(supplierService, times(1)).createSupplier(any(SupplierDTO.class));
//    }
//
//    @Test
//    @DisplayName("Should update supplier successfully")
//    void testUpdateSupplier() throws Exception {
//        when(supplierService.updateSupplier(eq(1L), any(SupplierDTO.class))).thenReturn(supplierDTO);
//
//        mockMvc.perform(put("/api/suppliers/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(supplierDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Test Supplier"));
//
//        verify(supplierService, times(1)).updateSupplier(eq(1L), any(SupplierDTO.class));
//    }
//
//    @Test
//    @DisplayName("Should get supplier by ID")
//    void testGetSupplierById() throws Exception {
//        when(supplierService.getSupplier(1L)).thenReturn(supplierDTO);
//
//        mockMvc.perform(get("/api/suppliers/1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Test Supplier"))
//                .andExpect(jsonPath("$.idSupplier").value(1));
//
//        verify(supplierService, times(1)).getSupplier(1L);
//    }
//
//    @Test
//    @DisplayName("Should get all suppliers with pagination")
//    void testGetAllSuppliers() throws Exception {
//        Page<SupplierDTO> page = new PageImpl<>(Arrays.asList(supplierDTO), PageRequest.of(0, 10), 1);
//        when(supplierService.getAllSuppliers(any())).thenReturn(page);
//
//        mockMvc.perform(get("/api/suppliers")
//                .param("page", "0")
//                .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"));
//
//        verify(supplierService, times(1)).getAllSuppliers(any());
//    }
//
//    @Test
//    @DisplayName("Should search suppliers by name")
//    void testSearchSuppliersByName() throws Exception {
//        Page<SupplierDTO> page = new PageImpl<>(Arrays.asList(supplierDTO), PageRequest.of(0, 10), 1);
//        when(supplierService.searchSuppliersByName(anyString(), any())).thenReturn(page);
//
//        mockMvc.perform(get("/api/suppliers/search")
//                .param("name", "Test")
//                .param("page", "0")
//                .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"));
//
//        verify(supplierService, times(1)).searchSuppliersByName(anyString(), any());
//    }
//
//    @Test
//    @DisplayName("Should delete supplier successfully")
//    void testDeleteSupplier() throws Exception {
//        doNothing().when(supplierService).deleteSupplier(1L);
//
//        mockMvc.perform(delete("/api/suppliers/1"))
//                .andExpect(status().isNoContent());
//
//        verify(supplierService, times(1)).deleteSupplier(1L);
//    }
//}
//
