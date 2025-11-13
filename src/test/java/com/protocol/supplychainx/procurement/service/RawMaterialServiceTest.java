package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.mapper.RawMaterialMapper;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.service.impl.RawMaterialService;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RawMaterialServiceTest {

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RawMaterialMapper rawMaterialMapper;

    @InjectMocks
    private RawMaterialService rawMaterialService;

    private RawMaterialDTO rawMaterialDTO;
    private RawMaterial rawMaterial;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .idSupplier(1L)
                .name("Test Supplier")
                .contact("test@supplier.com")
                .rating(4.5)
                .leadTime(7)
                .build();

        rawMaterialDTO = RawMaterialDTO.builder()
                .idMaterial(1L)
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .supplierIds(Set.of(1L))
                .build();

        rawMaterial = RawMaterial.builder()
                .idMaterial(1L)
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .suppliers(new HashSet<>(Set.of(supplier)))
                .build();
    }

    @Test
    @DisplayName("Should create raw material successfully")
    void testCreateRawMaterial_Success() {
        // Arrange
        when(rawMaterialMapper.toEntity(any(RawMaterialDTO.class))).thenReturn(rawMaterial);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        RawMaterialDTO result = rawMaterialService.createRawMaterial(rawMaterialDTO);

        // Assert
        assertNotNull(result);
        assertEquals(rawMaterialDTO.getName(), result.getName());
        assertEquals(rawMaterialDTO.getStock(), result.getStock());
        verify(rawMaterialRepository, times(1)).save(any(RawMaterial.class));
        verify(supplierRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when creating material with invalid supplier")
    void testCreateRawMaterial_SupplierNotFound() {
        // Arrange
        when(rawMaterialMapper.toEntity(any(RawMaterialDTO.class))).thenReturn(rawMaterial);
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            rawMaterialService.createRawMaterial(rawMaterialDTO)
        );
        verify(rawMaterialRepository, never()).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should update raw material successfully")
    void testUpdateRawMaterial_Success() {
        // Arrange
        RawMaterialDTO updateDTO = RawMaterialDTO.builder()
                .name("Steel Updated")
                .stock(150)
                .stockMin(30)
                .unit("kg")
                .supplierIds(Set.of(1L))
                .build();

        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(updateDTO);

        // Act
        RawMaterialDTO result = rawMaterialService.updateRawMaterial(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(rawMaterialRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should throw RawMaterialNotFoundException when updating non-existent material")
    void testUpdateRawMaterial_NotFound() {
        // Arrange
        when(rawMaterialRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RawMaterialNotFoundException.class, () -> 
            rawMaterialService.updateRawMaterial(999L, rawMaterialDTO)
        );
        verify(rawMaterialRepository, never()).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should get raw material by ID successfully")
    void testGetRawMaterial_Success() {
        // Arrange
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        RawMaterialDTO result = rawMaterialService.getRawMaterial(1L);

        // Assert
        assertNotNull(result);
        assertEquals(rawMaterialDTO.getName(), result.getName());
        verify(rawMaterialRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw RawMaterialNotFoundException when getting non-existent material")
    void testGetRawMaterial_NotFound() {
        // Arrange
        when(rawMaterialRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RawMaterialNotFoundException.class, () -> 
            rawMaterialService.getRawMaterial(999L)
        );
    }

    @Test
    @DisplayName("Should get all raw materials with pagination")
    void testGetAllRawMaterials_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<RawMaterial> materials = Arrays.asList(rawMaterial);
        Page<RawMaterial> materialPage = new PageImpl<>(materials, pageable, materials.size());
        
        when(rawMaterialRepository.findAll(pageable)).thenReturn(materialPage);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        Page<RawMaterialDTO> result = rawMaterialService.getAllRawMaterials(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(rawMaterialRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should search raw materials by name")
    void testSearchRawMaterialsByName_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<RawMaterial> materials = Arrays.asList(rawMaterial);
        Page<RawMaterial> materialPage = new PageImpl<>(materials, pageable, materials.size());
        
        when(rawMaterialRepository.findByNameContainingIgnoreCase("Steel", pageable)).thenReturn(materialPage);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        Page<RawMaterialDTO> result = rawMaterialService.searchRawMaterialsByName("Steel", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(rawMaterialRepository, times(1)).findByNameContainingIgnoreCase("Steel", pageable);
    }

    @Test
    @DisplayName("Should get raw materials below minimum stock")
    void testGetRawMaterialsBelowMinimumStock_Success() {
        // Arrange
        RawMaterial lowStockMaterial = RawMaterial.builder()
                .idMaterial(2L)
                .name("Copper")
                .stock(10)
                .stockMin(20)
                .unit("kg")
                .build();
        
        List<RawMaterial> materials = Arrays.asList(lowStockMaterial);
        
        when(rawMaterialRepository.findByStockLessThanStockMin()).thenReturn(materials);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        List<RawMaterialDTO> result = rawMaterialService.getAllRawMaterialsBelowMinimumStock();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rawMaterialRepository, times(1)).findByStockLessThanStockMin();
    }

    @Test
    @DisplayName("Should delete raw material successfully")
    void testDeleteRawMaterial_Success() {
        // Arrange
        when(rawMaterialRepository.existsById(1L)).thenReturn(true);
        doNothing().when(rawMaterialRepository).deleteById(1L);

        // Act
        rawMaterialService.deleteRawMaterial(1L);

        // Assert
        verify(rawMaterialRepository, times(1)).existsById(1L);
        verify(rawMaterialRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw RawMaterialNotFoundException when deleting non-existent material")
    void testDeleteRawMaterial_NotFound() {
        // Arrange
        when(rawMaterialRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(RawMaterialNotFoundException.class, () -> 
            rawMaterialService.deleteRawMaterial(999L)
        );
        verify(rawMaterialRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should add supplier to material successfully")
    void testAddSupplierToMaterial_Success() {
        // Arrange
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.save(any(RawMaterial.class))).thenReturn(rawMaterial);
        when(rawMaterialMapper.toDTO(any(RawMaterial.class))).thenReturn(rawMaterialDTO);

        // Act
        RawMaterialDTO result = rawMaterialService.addSupplierToMaterial(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(rawMaterialRepository, times(1)).findById(1L);
        verify(supplierRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).save(any(RawMaterial.class));
    }

    @Test
    @DisplayName("Should throw exceptions when adding invalid supplier to material")
    void testAddSupplierToMaterial_InvalidSupplier() {
        // Arrange
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            rawMaterialService.addSupplierToMaterial(1L, 999L)
        );
        verify(rawMaterialRepository, never()).save(any(RawMaterial.class));
    }
}

