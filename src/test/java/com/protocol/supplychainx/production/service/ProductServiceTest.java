package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.common.exceptions.production.ProductHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.production.dto.ProductDTO;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.mapper.ProductMapper;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.production.repository.ProductionOrderRepository;
import com.protocol.supplychainx.production.service.impl.ProductService;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private ProductDTO productDTO;
    private Product product;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .idProduct(1L)
                .name("Widget A")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        product = Product.builder()
                .idProduct(1L)
                .name("Widget A")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Arrange
        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        ProductDTO result = productService.createProduct(productDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        assertEquals(productDTO.getCost(), result.getCost());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Arrange
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Widget A Updated")
                .productionTime(7)
                .cost(150.0)
                .stock(75)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(updateDTO);

        // Act
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating non-existent product")
    void testUpdateProduct_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.updateProduct(999L, productDTO)
        );
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        ProductDTO result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(productDTO.getName(), result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when getting non-existent product")
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.getProductById(999L)
        );
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        Page<ProductDTO> result = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should search products by name")
    void testSearchProductsByName_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        
        when(productRepository.findByNameContainingIgnoreCase("Widget", pageable)).thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        Page<ProductDTO> result = productService.searchProductsByName("Widget", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByNameContainingIgnoreCase("Widget", pageable);
    }

    @Test
    @DisplayName("Should delete product successfully when no active orders")
    void testDeleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productionOrderRepository.countByProductIdProductAndStatusIn(
                eq(1L), 
                anyList()
        )).thenReturn(0L);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productionOrderRepository, times(1)).countByProductIdProductAndStatusIn(eq(1L), anyList());
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
    void testDeleteProduct_NotFound() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productService.deleteProduct(999L)
        );
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw ProductHasActiveOrdersException when deleting product with active orders")
    void testDeleteProduct_HasActiveOrders() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productionOrderRepository.countByProductIdProductAndStatusIn(
                eq(1L), 
                anyList()
        )).thenReturn(3L);

        // Act & Assert
        assertThrows(ProductHasActiveOrdersException.class, () -> 
            productService.deleteProduct(1L)
        );
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should check if product exists by name")
    void testExistsByName_Success() {
        // Arrange
        when(productRepository.existsByName("Widget A")).thenReturn(true);

        // Act
        boolean result = productService.existsByName("Widget A");

        // Assert
        assertTrue(result);
        verify(productRepository, times(1)).existsByName("Widget A");
    }

    @Test
    @DisplayName("Should return false when product does not exist by name")
    void testExistsByName_NotFound() {
        // Arrange
        when(productRepository.existsByName("Non-existent Product")).thenReturn(false);

        // Act
        boolean result = productService.existsByName("Non-existent Product");

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).existsByName("Non-existent Product");
    }
}

