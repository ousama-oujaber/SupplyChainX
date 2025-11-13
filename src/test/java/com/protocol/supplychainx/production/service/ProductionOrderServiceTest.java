package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.common.exceptions.production.InsufficientMaterialsException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.common.exceptions.production.ProductionOrderCannotBeCancelledException;
import com.protocol.supplychainx.common.exceptions.production.ProductionOrderNotFoundException;
import com.protocol.supplychainx.production.dto.ProductionOrderDTO;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.entity.ProductionOrder;
import com.protocol.supplychainx.production.mapper.ProductionOrderMapper;
import com.protocol.supplychainx.production.repository.BillOfMaterialRepository;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.production.repository.ProductionOrderRepository;
import com.protocol.supplychainx.production.service.impl.ProductionOrderService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionOrderServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BillOfMaterialRepository billOfMaterialRepository;

    @Mock
    private ProductionOrderMapper productionOrderMapper;

    @Mock
    private IBillOfMaterialService billOfMaterialService;

    @InjectMocks
    private ProductionOrderService productionOrderService;

    private ProductionOrderDTO productionOrderDTO;
    private ProductionOrder productionOrder;
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .idProduct(1L)
                .name("Widget A")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        productionOrderDTO = ProductionOrderDTO.builder()
                .idOrder(1L)
                .productId(1L)
                .quantity(10)
                .status(ProductionOrderStatus.EN_ATTENTE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .isPriority(false)
                .build();

        productionOrder = ProductionOrder.builder()
                .idOrder(1L)
                .product(product)
                .quantity(10)
                .status(ProductionOrderStatus.EN_ATTENTE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .isPriority(false)
                .build();
    }

    @Test
    @DisplayName("Should create production order successfully when materials available")
    void testCreateProductionOrder_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(billOfMaterialService.checkMaterialsAvailability(1L, 10)).thenReturn(true);
        when(productionOrderMapper.toEntity(any(ProductionOrderDTO.class))).thenReturn(productionOrder);
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        ProductionOrderDTO result = productionOrderService.createProductionOrder(productionOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(productionOrderDTO.getQuantity(), result.getQuantity());
        verify(productionOrderRepository, times(1)).save(any(ProductionOrder.class));
        verify(billOfMaterialService, times(1)).checkMaterialsAvailability(1L, 10);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when creating order with invalid product")
    void testCreateProductionOrder_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productionOrderService.createProductionOrder(productionOrderDTO)
        );
        verify(productionOrderRepository, never()).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should throw InsufficientMaterialsException when materials not available")
    void testCreateProductionOrder_InsufficientMaterials() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(billOfMaterialService.checkMaterialsAvailability(1L, 10)).thenReturn(false);

        // Act & Assert
        assertThrows(InsufficientMaterialsException.class, () -> 
            productionOrderService.createProductionOrder(productionOrderDTO)
        );
        verify(productionOrderRepository, never()).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should update production order successfully")
    void testUpdateProductionOrder_Success() {
        // Arrange
        ProductionOrderDTO updateDTO = ProductionOrderDTO.builder()
                .quantity(15)
                .status(ProductionOrderStatus.EN_PRODUCTION)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(12))
                .isPriority(true)
                .build();

        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(updateDTO);

        // Act
        ProductionOrderDTO result = productionOrderService.updateProductionOrder(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should throw ProductionOrderNotFoundException when updating non-existent order")
    void testUpdateProductionOrder_NotFound() {
        // Arrange
        when(productionOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductionOrderNotFoundException.class, () -> 
            productionOrderService.updateProductionOrder(999L, productionOrderDTO)
        );
        verify(productionOrderRepository, never()).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should get production order by ID successfully")
    void testGetProductionOrderById_Success() {
        // Arrange
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);
        when(billOfMaterialService.checkMaterialsAvailability(1L, 10)).thenReturn(true);

        // Act
        ProductionOrderDTO result = productionOrderService.getProductionOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(productionOrderDTO.getIdOrder(), result.getIdOrder());
        verify(productionOrderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get all production orders with pagination")
    void testGetAllProductionOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        Page<ProductionOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(productionOrderRepository.findAll(pageable)).thenReturn(orderPage);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        Page<ProductionOrderDTO> result = productionOrderService.getAllProductionOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productionOrderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get production orders by status")
    void testGetProductionOrdersByStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        Page<ProductionOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(productionOrderRepository.findByStatus(ProductionOrderStatus.EN_ATTENTE, pageable)).thenReturn(orderPage);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        Page<ProductionOrderDTO> result = productionOrderService.getProductionOrdersByStatus(
            ProductionOrderStatus.EN_ATTENTE, pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productionOrderRepository, times(1)).findByStatus(ProductionOrderStatus.EN_ATTENTE, pageable);
    }

    @Test
    @DisplayName("Should get production orders by product")
    void testGetProductionOrdersByProduct_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        Page<ProductionOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productionOrderRepository.findByProductIdProduct(1L, pageable)).thenReturn(orderPage);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        Page<ProductionOrderDTO> result = productionOrderService.getProductionOrdersByProduct(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productionOrderRepository, times(1)).findByProductIdProduct(1L, pageable);
    }

    @Test
    @DisplayName("Should throw exception when getting orders by non-existent product")
    void testGetProductionOrdersByProduct_ProductNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            productionOrderService.getProductionOrdersByProduct(999L, pageable)
        );
    }

    @Test
    @DisplayName("Should get priority production orders")
    void testGetPriorityProductionOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        productionOrder.setIsPriority(true);
        List<ProductionOrder> orders = Arrays.asList(productionOrder);
        Page<ProductionOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(productionOrderRepository.findByIsPriorityTrue(pageable)).thenReturn(orderPage);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        Page<ProductionOrderDTO> result = productionOrderService.getPriorityProductionOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productionOrderRepository, times(1)).findByIsPriorityTrue(pageable);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(productionOrder));
        when(productionOrderRepository.save(any(ProductionOrder.class))).thenReturn(productionOrder);
        when(productionOrderMapper.toDTO(any(ProductionOrder.class))).thenReturn(productionOrderDTO);

        // Act
        ProductionOrderDTO result = productionOrderService.updateOrderStatus(1L, ProductionOrderStatus.EN_PRODUCTION);

        // Assert
        assertNotNull(result);
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).save(any(ProductionOrder.class));
    }

    @Test
    @DisplayName("Should cancel production order successfully when cancellable")
    void testCancelProductionOrder_Success() {
        // Arrange
    productionOrder.setStatus(ProductionOrderStatus.EN_ATTENTE); // Set to cancellable status
    doReturn(Optional.of(productionOrder)).when(productionOrderRepository).findById(1L);
        doNothing().when(productionOrderRepository).deleteById(1L);

        // Act
        productionOrderService.cancelProductionOrder(1L);

        // Assert
        verify(productionOrderRepository, times(1)).findById(1L);
        verify(productionOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-cancellable order")
    void testCancelProductionOrder_CannotBeCancelled() {
        // Arrange
    productionOrder.setStatus(ProductionOrderStatus.TERMINE);
    doReturn(Optional.of(productionOrder)).when(productionOrderRepository).findById(1L);

        // Act & Assert
        assertThrows(ProductionOrderCannotBeCancelledException.class, () -> 
            productionOrderService.cancelProductionOrder(1L)
        );
        verify(productionOrderRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should calculate estimated production time")
    void testCalculateEstimatedProductionTime_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Integer result = productionOrderService.calculateEstimatedProductionTime(1L, 10);

        // Assert
        assertNotNull(result);
        assertEquals(50, result); // 5 hours * 10 units
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return null when product has no production time")
    void testCalculateEstimatedProductionTime_NoProductionTime() {
        // Arrange
        product.setProductionTime(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Integer result = productionOrderService.calculateEstimatedProductionTime(1L, 10);

        // Assert
        assertNull(result);
        verify(productRepository, times(1)).findById(1L);
    }
}

