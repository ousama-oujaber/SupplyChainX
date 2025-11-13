package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplyOrderCannotBeDeletedException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplyOrderNotFoundException;
import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.entity.SupplyOrder;
import com.protocol.supplychainx.procurement.mapper.SupplyOrderMapper;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.repository.SupplyOrderRepository;
import com.protocol.supplychainx.procurement.service.impl.SupplyOrderService;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyOrderServiceTest {

    @Mock
    private SupplyOrderRepository supplyOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private RawMaterialRepository rawMaterialRepository;

    @Mock
    private SupplyOrderMapper supplyOrderMapper;

    @InjectMocks
    private SupplyOrderService supplyOrderService;

    private SupplyOrderDTO supplyOrderDTO;
    private SupplyOrder supplyOrder;
    private Supplier supplier;
    private RawMaterial rawMaterial;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .idSupplier(1L)
                .name("Test Supplier")
                .contact("test@supplier.com")
                .rating(4.5)
                .leadTime(7)
                .build();

        rawMaterial = RawMaterial.builder()
                .idMaterial(1L)
                .name("Steel")
                .stock(100)
                .stockMin(20)
                .unit("kg")
                .build();

        supplyOrderDTO = SupplyOrderDTO.builder()
                .idOrder(1L)
                .supplierId(1L)
                .materialIds(Set.of(1L))
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .status(SupplyOrderStatus.EN_ATTENTE)
                .build();

        supplyOrder = SupplyOrder.builder()
                .idOrder(1L)
                .supplier(supplier)
                .materials(new HashSet<>(Set.of(rawMaterial)))
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(7))
                .status(SupplyOrderStatus.EN_ATTENTE)
                .build();
    }

    @Test
    @DisplayName("Should create supply order successfully")
    void testCreateSupplyOrder_Success() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplyOrderMapper.toEntity(any(SupplyOrderDTO.class))).thenReturn(supplyOrder);
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(supplyOrderDTO);

        // Act
        SupplyOrderDTO result = supplyOrderService.createSupplyOrder(supplyOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(supplyOrderDTO.getSupplierId(), result.getSupplierId());
        verify(supplyOrderRepository, times(1)).save(any(SupplyOrder.class));
        verify(supplierRepository, times(1)).findById(1L);
        verify(rawMaterialRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw SupplierNotFoundException when creating order with invalid supplier")
    void testCreateSupplyOrder_SupplierNotFound() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            supplyOrderService.createSupplyOrder(supplyOrderDTO)
        );
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should throw RawMaterialNotFoundException when creating order with invalid material")
    void testCreateSupplyOrder_MaterialNotFound() {
        // Arrange
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RawMaterialNotFoundException.class, () -> 
            supplyOrderService.createSupplyOrder(supplyOrderDTO)
        );
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should update supply order successfully")
    void testUpdateSupplyOrder_Success() {
        // Arrange
        SupplyOrderDTO updateDTO = SupplyOrderDTO.builder()
                .supplierId(1L)
                .materialIds(Set.of(1L))
                .orderDate(LocalDate.now())
                .expectedDeliveryDate(LocalDate.now().plusDays(10))
                .status(SupplyOrderStatus.EN_COURS)
                .build();

        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        // Supplier stub not needed when ID matches existing order
        when(rawMaterialRepository.findById(1L)).thenReturn(Optional.of(rawMaterial));
        when(supplyOrderRepository.save(any(SupplyOrder.class))).thenReturn(supplyOrder);
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(updateDTO);

        // Act
        SupplyOrderDTO result = supplyOrderService.updateSupplyOrder(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(supplyOrderRepository, times(1)).findById(1L);
        verify(supplyOrderRepository, times(1)).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should throw SupplyOrderNotFoundException when updating non-existent order")
    void testUpdateSupplyOrder_NotFound() {
        // Arrange
        when(supplyOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplyOrderNotFoundException.class, () -> 
            supplyOrderService.updateSupplyOrder(999L, supplyOrderDTO)
        );
        verify(supplyOrderRepository, never()).save(any(SupplyOrder.class));
    }

    @Test
    @DisplayName("Should get supply order by ID successfully")
    void testGetSupplyOrder_Success() {
        // Arrange
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(supplyOrderDTO);

        // Act
        SupplyOrderDTO result = supplyOrderService.getSupplyOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(supplyOrderDTO.getIdOrder(), result.getIdOrder());
        verify(supplyOrderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should get all supply orders with pagination")
    void testGetAllSupplyOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(supplyOrderRepository.findAll(pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(supplyOrderDTO);

        // Act
        Page<SupplyOrderDTO> result = supplyOrderService.getAllSupplyOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(supplyOrderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get supply orders by status")
    void testGetSupplyOrdersByStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(supplyOrderRepository.findByStatus(SupplyOrderStatus.EN_ATTENTE, pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(supplyOrderDTO);

        // Act
        Page<SupplyOrderDTO> result = supplyOrderService.getSupplyOrdersByStatus(SupplyOrderStatus.EN_ATTENTE, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(supplyOrderRepository, times(1)).findByStatus(SupplyOrderStatus.EN_ATTENTE, pageable);
    }

    @Test
    @DisplayName("Should get supply orders by supplier")
    void testGetSupplyOrdersBySupplier_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<SupplyOrder> orders = Arrays.asList(supplyOrder);
        Page<SupplyOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(supplyOrderRepository.findBySupplierIdSupplier(1L, pageable)).thenReturn(orderPage);
        when(supplyOrderMapper.toDTO(any(SupplyOrder.class))).thenReturn(supplyOrderDTO);

        // Act
        Page<SupplyOrderDTO> result = supplyOrderService.getSupplyOrdersBySupplier(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(supplyOrderRepository, times(1)).findBySupplierIdSupplier(1L, pageable);
    }

    @Test
    @DisplayName("Should throw exception when getting orders by non-existent supplier")
    void testGetSupplyOrdersBySupplier_SupplierNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(SupplierNotFoundException.class, () -> 
            supplyOrderService.getSupplyOrdersBySupplier(999L, pageable)
        );
    }

    @Test
    @DisplayName("Should delete supply order successfully when cancellable")
    void testDeleteSupplyOrder_Success() {
        // Arrange
        supplyOrder.setStatus(SupplyOrderStatus.EN_ATTENTE); // Status must be EN_ATTENTE to be deletable
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));
        doNothing().when(supplyOrderRepository).deleteById(1L);

        // Act
        supplyOrderService.deleteSupplyOrder(1L);

        // Assert
        verify(supplyOrderRepository, times(1)).findById(1L);
        verify(supplyOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-deletable order")
    void testDeleteSupplyOrder_CannotBeDeleted() {
        // Arrange
        supplyOrder.setStatus(SupplyOrderStatus.EN_COURS); // Status EN_COURS makes it non-deletable
        when(supplyOrderRepository.findById(1L)).thenReturn(Optional.of(supplyOrder));

        // Act & Assert
        assertThrows(SupplyOrderCannotBeDeletedException.class, () -> 
            supplyOrderService.deleteSupplyOrder(1L)
        );
        verify(supplyOrderRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw SupplyOrderNotFoundException when deleting non-existent order")
    void testDeleteSupplyOrder_NotFound() {
        // Arrange
        when(supplyOrderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SupplyOrderNotFoundException.class, () -> 
            supplyOrderService.deleteSupplyOrder(999L)
        );
        verify(supplyOrderRepository, never()).deleteById(anyLong());
    }
}

