package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.common.exceptions.production.BillOfMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.production.dto.BillOfMaterialDTO;
import com.protocol.supplychainx.production.entity.BillOfMaterial;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.mapper.BillOfMaterialMapper;
import com.protocol.supplychainx.production.repository.BillOfMaterialRepository;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.production.service.impl.BillOfMaterialService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillOfMaterialServiceTest {

    @Mock
    private BillOfMaterialRepository billOfMaterialRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private BillOfMaterialMapper billOfMaterialMapper;

    @InjectMocks
    private BillOfMaterialService billOfMaterialService;

    private BillOfMaterialDTO billOfMaterialDTO;
    private BillOfMaterial billOfMaterial;
    private Product product;
    private RawMaterial rawMaterial;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .idProduct(1L)
                .name("Widget A")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        rawMaterial = RawMaterial.builder()
                .idMaterial(1L)
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .build();

        billOfMaterialDTO = BillOfMaterialDTO.builder()
                .idBOM(1L)
                .productId(1L)
                .materialId(1L)
                .quantity(5)
                .build();

        billOfMaterial = BillOfMaterial.builder()
                .idBOM(1L)
                .product(product)
                .material(rawMaterial)
                .quantity(5)
                .build();
    }

    @Test
    @DisplayName("Should create bill of material successfully")
    void testCreateBillOfMaterial_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(billOfMaterialMapper.toEntity(any(BillOfMaterialDTO.class))).thenReturn(billOfMaterial);
        when(billOfMaterialRepository.save(any(BillOfMaterial.class))).thenReturn(billOfMaterial);
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(billOfMaterialDTO);

        // Act
        BillOfMaterialDTO result = billOfMaterialService.createBillOfMaterial(billOfMaterialDTO);

        // Assert
        assertNotNull(result);
        assertEquals(billOfMaterialDTO.getQuantity(), result.getQuantity());
        verify(billOfMaterialRepository, times(1)).save(any(BillOfMaterial.class));
        verify(productRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when creating BOM with invalid product")
    void testCreateBillOfMaterial_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            billOfMaterialService.createBillOfMaterial(billOfMaterialDTO)
        );
        verify(billOfMaterialRepository, never()).save(any(BillOfMaterial.class));
    }

    @Test
    @DisplayName("Should throw RawMaterialNotFoundException when creating BOM with invalid material")
    void testCreateBillOfMaterial_MaterialNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RawMaterialNotFoundException.class, () -> 
            billOfMaterialService.createBillOfMaterial(billOfMaterialDTO)
        );
        verify(billOfMaterialRepository, never()).save(any(BillOfMaterial.class));
    }

    @Test
    @DisplayName("Should update bill of material successfully")
    void testUpdateBillOfMaterial_Success() {
        // Arrange
        BillOfMaterialDTO updateDTO = BillOfMaterialDTO.builder()
                .quantity(10)
                .build();

        when(billOfMaterialRepository.findById(1L)).thenReturn(Optional.of(billOfMaterial));
        when(billOfMaterialRepository.save(any(BillOfMaterial.class))).thenReturn(billOfMaterial);
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(updateDTO);

        // Act
        BillOfMaterialDTO result = billOfMaterialService.updateBillOfMaterial(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(billOfMaterialRepository, times(1)).findById(1L);
        verify(billOfMaterialRepository, times(1)).save(any(BillOfMaterial.class));
    }

    @Test
    @DisplayName("Should throw BillOfMaterialNotFoundException when updating non-existent BOM")
    void testUpdateBillOfMaterial_NotFound() {
        // Arrange
        when(billOfMaterialRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BillOfMaterialNotFoundException.class, () -> 
            billOfMaterialService.updateBillOfMaterial(999L, billOfMaterialDTO)
        );
        verify(billOfMaterialRepository, never()).save(any(BillOfMaterial.class));
    }

    @Test
    @DisplayName("Should get bill of material by ID successfully")
    void testGetBillOfMaterialById_Success() {
        // Arrange
        when(billOfMaterialRepository.findById(1L)).thenReturn(Optional.of(billOfMaterial));
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(billOfMaterialDTO);

        // Act
        BillOfMaterialDTO result = billOfMaterialService.getBillOfMaterialById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(billOfMaterialDTO.getIdBOM(), result.getIdBOM());
        verify(billOfMaterialRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw BillOfMaterialNotFoundException when getting non-existent BOM")
    void testGetBillOfMaterialById_NotFound() {
        // Arrange
        when(billOfMaterialRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BillOfMaterialNotFoundException.class, () -> 
            billOfMaterialService.getBillOfMaterialById(999L)
        );
    }

    @Test
    @DisplayName("Should get all bills of material with pagination")
    void testGetAllBillOfMaterials_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<BillOfMaterial> boms = Arrays.asList(billOfMaterial);
        Page<BillOfMaterial> bomPage = new PageImpl<>(boms, pageable, boms.size());
        
        when(billOfMaterialRepository.findAll(pageable)).thenReturn(bomPage);
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(billOfMaterialDTO);

        // Act
        Page<BillOfMaterialDTO> result = billOfMaterialService.getAllBillOfMaterials(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(billOfMaterialRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get bills of material by product")
    void testGetBillOfMaterialsByProduct_Success() {
        // Arrange
        List<BillOfMaterial> boms = Arrays.asList(billOfMaterial);
        
        when(productRepository.existsById(1L)).thenReturn(true);
        when(billOfMaterialRepository.findByProductIdProduct(1L)).thenReturn(boms);
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(billOfMaterialDTO);

        // Act
        List<BillOfMaterialDTO> result = billOfMaterialService.getBillOfMaterialsByProduct(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(billOfMaterialRepository, times(1)).findByProductIdProduct(1L);
    }

    @Test
    @DisplayName("Should throw exception when getting BOMs by non-existent product")
    void testGetBillOfMaterialsByProduct_ProductNotFound() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            billOfMaterialService.getBillOfMaterialsByProduct(999L)
        );
    }

    @Test
    @DisplayName("Should get bills of material by product with pagination")
    void testGetBillOfMaterialsByProductPaginated_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<BillOfMaterial> boms = Arrays.asList(billOfMaterial);
        Page<BillOfMaterial> bomPage = new PageImpl<>(boms, pageable, boms.size());
        
        when(productRepository.existsById(1L)).thenReturn(true);
        when(billOfMaterialRepository.findByProductIdProduct(1L, pageable)).thenReturn(bomPage);
        when(billOfMaterialMapper.toDTO(any(BillOfMaterial.class))).thenReturn(billOfMaterialDTO);

        // Act
        Page<BillOfMaterialDTO> result = billOfMaterialService.getBillOfMaterialsByProductPaginated(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(billOfMaterialRepository, times(1)).findByProductIdProduct(1L, pageable);
    }

    @Test
    @DisplayName("Should delete bill of material successfully")
    void testDeleteBillOfMaterial_Success() {
        // Arrange
        when(billOfMaterialRepository.existsById(1L)).thenReturn(true);
        doNothing().when(billOfMaterialRepository).deleteById(1L);

        // Act
        billOfMaterialService.deleteBillOfMaterial(1L);

        // Assert
        verify(billOfMaterialRepository, times(1)).existsById(1L);
        verify(billOfMaterialRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw BillOfMaterialNotFoundException when deleting non-existent BOM")
    void testDeleteBillOfMaterial_NotFound() {
        // Arrange
        when(billOfMaterialRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(BillOfMaterialNotFoundException.class, () -> 
            billOfMaterialService.deleteBillOfMaterial(999L)
        );
        verify(billOfMaterialRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should return true when materials are available")
    void testCheckMaterialsAvailability_Available() {
        // Arrange
        List<BillOfMaterial> boms = Arrays.asList(billOfMaterial);
        when(billOfMaterialRepository.findByProductIdProduct(1L)).thenReturn(boms);

        // Act
        boolean result = billOfMaterialService.checkMaterialsAvailability(1L, 10);

        // Assert
        assertTrue(result);
        verify(billOfMaterialRepository, times(1)).findByProductIdProduct(1L);
    }

    @Test
    @DisplayName("Should return false when materials are not available")
    void testCheckMaterialsAvailability_NotAvailable() {
        // Arrange
        rawMaterial.setStock(10); // Not enough for 5 * 10 = 50
        List<BillOfMaterial> boms = Arrays.asList(billOfMaterial);
        when(billOfMaterialRepository.findByProductIdProduct(1L)).thenReturn(boms);

        // Act
        boolean result = billOfMaterialService.checkMaterialsAvailability(1L, 20);

        // Assert
        assertFalse(result);
        verify(billOfMaterialRepository, times(1)).findByProductIdProduct(1L);
    }

    @Test
    @DisplayName("Should return false when no BOM found for product")
    void testCheckMaterialsAvailability_NoBOM() {
        // Arrange
        when(billOfMaterialRepository.findByProductIdProduct(1L)).thenReturn(Arrays.asList());

        // Act
        boolean result = billOfMaterialService.checkMaterialsAvailability(1L, 10);

        // Assert
        assertFalse(result);
        verify(billOfMaterialRepository, times(1)).findByProductIdProduct(1L);
    }
}

