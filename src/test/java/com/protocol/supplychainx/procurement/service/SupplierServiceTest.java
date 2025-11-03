package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.common.exceptions.procurement.SupplierHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.mapper.SupplierMapper;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.service.impl.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private SupplierDTO supplierDTO;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplierDTO = SupplierDTO.builder()
                .idSupplier(1L)
                .name("Supplier A")
                .contact("contact@supplierA.com, +1234567890")
                .rating(4.5)
                .leadTime(7)
                .build();

        supplier = Supplier.builder()
                .idSupplier(1L)
                .name("Supplier A")
                .contact("contact@supplierA.com, +1234567890")
                .rating(4.5)
                .leadTime(7)
                .build();
    }

    @Test
    @DisplayName("Should create supplier successfully")
    void testCreateSupplier_Success() {
        // Arrange
        when(supplierMapper.toEntity(any(SupplierDTO.class))).thenReturn(supplier);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(supplierDTO);

        // Act
        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);

        // Assert
        assertNotNull(createdSupplier);
        assertEquals(supplierDTO.getName(), createdSupplier.getName());
        assertEquals(supplierDTO.getContact(), createdSupplier.getContact());
        assertEquals(supplierDTO.getRating(), createdSupplier.getRating());
        assertEquals(supplierDTO.getLeadTime(), createdSupplier.getLeadTime());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
        verify(supplierMapper, times(1)).toEntity(any(SupplierDTO.class));
        verify(supplierMapper, times(1)).toDTO(any(Supplier.class));
    }

    @Test
    @DisplayName("Should update supplier successfully")
    void testUpdateSupplier_Success() {
        // Arrange
        Long supplierId = 1L;
        SupplierDTO updateDTO = SupplierDTO.builder()
                .name("Updated Supplier")
                .contact("updated@supplier.com")
                .rating(5.0)
                .leadTime(5)
                .build();

        Supplier updatedSupplier = Supplier.builder()
                .idSupplier(supplierId)
                .name("Updated Supplier")
                .contact("updated@supplier.com")
                .rating(5.0)
                .leadTime(5)
                .build();

        SupplierDTO updatedDTO = SupplierDTO.builder()
                .idSupplier(supplierId)
                .name("Updated Supplier")
                .contact("updated@supplier.com")
                .rating(5.0)
                .leadTime(5)
                .build();

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updatedSupplier);
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(updatedDTO);

        // Act
        SupplierDTO result = supplierService.updateSupplier(supplierId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDTO.getName(), result.getName());
        assertEquals(updatedDTO.getContact(), result.getContact());
        assertEquals(updatedDTO.getRating(), result.getRating());
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when updating non-existent supplier")
    void testUpdateSupplier_NotFound() {
        // Arrange
        Long supplierId = 999L;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplierNotFoundException.class,
                () -> supplierService.updateSupplier(supplierId, supplierDTO));
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should get supplier successfully")
    void testGetSupplier_Success() {
        Long supplierId = 1L;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.getSupplier(supplierId);

        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        verify(supplierRepository, times(1)).findById(supplierId);
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when getting non-existent supplier")
    void testGetSupplier_NotFound() {
        Long supplierId = 999L;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class,
                () -> supplierService.getSupplier(supplierId));
    }

    @Test
    @DisplayName("Should get all suppliers successfully")
    void testGetAllSuppliers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(java.util.Collections.singletonList(supplier));
        when(supplierRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(supplierDTO);

        // Act
        Page<SupplierDTO> result = supplierService.getAllSuppliers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(supplierRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should search suppliers by name successfully")
    void testSearchSuppliersByName_Success() {
        // Arrange
        String searchName = "Supplier";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(java.util.Collections.singletonList(supplier));
        when(supplierRepository.findByNameContainingIgnoreCase(searchName, pageable)).thenReturn(supplierPage);
        when(supplierMapper.toDTO(any(Supplier.class))).thenReturn(supplierDTO);

        // Act
        Page<SupplierDTO> result = supplierService.searchSuppliersByName(searchName, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(supplierRepository, times(1)).findByNameContainingIgnoreCase(searchName, pageable);
    }

    @Test
    @DisplayName("Should delete supplier successfully when no active orders")
    void testDeleteSupplier_Success() {
        // Arrange
        Long supplierId = 1L;
        when(supplierRepository.existsById(supplierId)).thenReturn(true);
        when(supplierRepository.countActiveOrdersBySupplier(eq(supplierId), anyList())).thenReturn(0L);

        // Act
        supplierService.deleteSupplier(supplierId);

        // Assert
        verify(supplierRepository, times(1)).existsById(supplierId);
        verify(supplierRepository, times(1)).countActiveOrdersBySupplier(eq(supplierId), anyList());
        verify(supplierRepository, times(1)).deleteById(supplierId);
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when deleting non-existent supplier")
    void testDeleteSupplier_NotFound() {
        // Arrange
        Long supplierId = 999L;
        when(supplierRepository.existsById(supplierId)).thenReturn(false);

        // Act & Assert
        assertThrows(SupplierNotFoundException.class,
                () -> supplierService.deleteSupplier(supplierId));
        verify(supplierRepository, never()).deleteById(supplierId);
    }

    @Test
    @DisplayName("Should throw SupplierHasActiveOrdersException when deleting supplier with active orders")
    void testDeleteSupplier_HasActiveOrders() {
        // Arrange
        Long supplierId = 1L;
        when(supplierRepository.existsById(supplierId)).thenReturn(true);
        when(supplierRepository.countActiveOrdersBySupplier(eq(supplierId), anyList())).thenReturn(3L);

        // Act & Assert
        assertThrows(SupplierHasActiveOrdersException.class,
                () -> supplierService.deleteSupplier(supplierId));
        verify(supplierRepository, never()).deleteById(supplierId);
    }
}
