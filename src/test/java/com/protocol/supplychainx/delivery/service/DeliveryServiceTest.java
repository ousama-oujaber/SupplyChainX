package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.common.enums.DeliveryStatus;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerOrderNotFoundException;
import com.protocol.supplychainx.common.exceptions.delivery.DeliveryNotFoundException;
import com.protocol.supplychainx.delivery.dto.DeliveryDTO;
import com.protocol.supplychainx.delivery.entity.CustomerOrder;
import com.protocol.supplychainx.delivery.entity.Delivery;
import com.protocol.supplychainx.delivery.mapper.DeliveryMapper;
import com.protocol.supplychainx.delivery.repository.CustomerOrderRepository;
import com.protocol.supplychainx.delivery.repository.DeliveryRepository;
import com.protocol.supplychainx.delivery.service.impl.DeliveryService;
import com.protocol.supplychainx.production.entity.Product;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private DeliveryMapper deliveryMapper;

    @InjectMocks
    private DeliveryService deliveryService;

    private DeliveryDTO deliveryDTO;
    private Delivery delivery;
    private CustomerOrder customerOrder;
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .idProduct(1L)
                .name("Widget")
                .productionTime(5)
                .cost(100.0)
                .stock(40)
                .build();

        customerOrder = CustomerOrder.builder()
                .idOrder(1L)
                .product(product)
                .quantity(2)
                .status(CustomerOrderStatus.EN_PREPARATION)
                .build();

        deliveryDTO = DeliveryDTO.builder()
                .orderId(1L)
                .vehicle("Truck 1")
                .driver("Alex")
                .deliveryDate(LocalDate.now())
                .build();
        deliveryDTO.setStatus(null);
        deliveryDTO.setCost(null);

        delivery = Delivery.builder()
                .vehicle("Truck 1")
                .driver("Alex")
                .deliveryDate(deliveryDTO.getDeliveryDate())
                .build();
    }

    @Test
    @DisplayName("Should create delivery with defaults and update order status")
    void testCreateDelivery_DefaultsAndOrderStatusUpdated() {
    when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));
    when(deliveryMapper.toEntity(any(DeliveryDTO.class))).thenReturn(delivery);
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            saved.setIdDelivery(10L);
            return saved;
        });
        when(customerOrderRepository.save(customerOrder)).thenReturn(customerOrder);
        when(deliveryMapper.toDTO(any(Delivery.class))).thenReturn(DeliveryDTO.builder()
                .idDelivery(10L)
                .orderId(1L)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(deliveryDTO.getDeliveryDate())
                .cost(70.0)
                .build());

        DeliveryDTO result = deliveryService.createDelivery(deliveryDTO);

        assertNotNull(result);
        assertEquals(CustomerOrderStatus.EN_ROUTE, customerOrder.getStatus());
        assertEquals(DeliveryStatus.PLANIFIEE, delivery.getStatus());
        assertEquals(70.0, delivery.getCost(), 0.001);
        verify(customerOrderRepository).save(customerOrder);
        verify(deliveryRepository).save(delivery);
    }

    @Test
    @DisplayName("Should throw exception when creating delivery for unknown order")
    void testCreateDelivery_OrderNotFound() {
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CustomerOrderNotFoundException.class, () -> deliveryService.createDelivery(deliveryDTO));
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Should update delivery to in progress and sync order status")
    void testUpdateDelivery_StatusEnCoursUpdatesOrder() {
        Delivery existingDelivery = Delivery.builder()
                .idDelivery(5L)
                .order(customerOrder)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(delivery.getDeliveryDate())
                .vehicle("Truck 1")
                .driver("Alex")
                .cost(70.0)
                .build();

        DeliveryDTO updateDTO = DeliveryDTO.builder()
                .status(DeliveryStatus.EN_COURS)
                .vehicle("Van")
                .driver("Sam")
                .deliveryDate(deliveryDTO.getDeliveryDate().plusDays(1))
                .cost(85.0)
                .build();

        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(existingDelivery));
        when(customerOrderRepository.save(customerOrder)).thenReturn(customerOrder);
        when(deliveryRepository.save(existingDelivery)).thenReturn(existingDelivery);
        when(deliveryMapper.toDTO(existingDelivery)).thenReturn(updateDTO);

        DeliveryDTO result = deliveryService.updateDelivery(5L, updateDTO);

        assertNotNull(result);
        assertEquals(CustomerOrderStatus.EN_ROUTE, customerOrder.getStatus());
        assertEquals(DeliveryStatus.EN_COURS, existingDelivery.getStatus());
        verify(customerOrderRepository).save(customerOrder);
        verify(deliveryRepository).save(existingDelivery);
    }

    @Test
    @DisplayName("Should update delivery to delivered and mark order delivered")
    void testUpdateDelivery_StatusLivreeUpdatesOrder() {
        customerOrder.setStatus(CustomerOrderStatus.EN_ROUTE);

        Delivery existingDelivery = Delivery.builder()
                .idDelivery(6L)
                .order(customerOrder)
                .status(DeliveryStatus.EN_COURS)
                .deliveryDate(delivery.getDeliveryDate())
                .cost(90.0)
                .build();

        DeliveryDTO updateDTO = DeliveryDTO.builder()
                .status(DeliveryStatus.LIVREE)
                .cost(95.0)
                .build();

        when(deliveryRepository.findById(6L)).thenReturn(Optional.of(existingDelivery));
        when(customerOrderRepository.save(customerOrder)).thenReturn(customerOrder);
        when(deliveryRepository.save(existingDelivery)).thenReturn(existingDelivery);
        when(deliveryMapper.toDTO(existingDelivery)).thenReturn(updateDTO);

        DeliveryDTO result = deliveryService.updateDelivery(6L, updateDTO);

        assertNotNull(result);
        assertEquals(CustomerOrderStatus.LIVREE, customerOrder.getStatus());
        assertEquals(DeliveryStatus.LIVREE, existingDelivery.getStatus());
        verify(customerOrderRepository).save(customerOrder);
    }

    @Test
    @DisplayName("Should retrieve delivery by ID")
    void testGetDelivery_Success() {
        Delivery existingDelivery = Delivery.builder()
                .idDelivery(7L)
                .order(customerOrder)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(delivery.getDeliveryDate())
                .build();

        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryMapper.toDTO(existingDelivery)).thenReturn(DeliveryDTO.builder()
                .idDelivery(7L)
                .orderId(1L)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(existingDelivery.getDeliveryDate())
                .build());

        DeliveryDTO result = deliveryService.getDelivery(7L);

        assertNotNull(result);
        verify(deliveryRepository).findById(7L);
    }

    @Test
    @DisplayName("Should throw when delivery ID does not exist")
    void testGetDelivery_NotFound() {
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DeliveryNotFoundException.class, () -> deliveryService.getDelivery(99L));
    }

    @Test
    @DisplayName("Should fetch delivery by order ID")
    void testGetDeliveryByOrderId_Success() {
        Delivery existingDelivery = Delivery.builder()
                .idDelivery(8L)
                .order(customerOrder)
                .status(DeliveryStatus.EN_COURS)
                .deliveryDate(delivery.getDeliveryDate())
                .build();

        when(deliveryRepository.findByOrderIdOrder(1L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryMapper.toDTO(existingDelivery)).thenReturn(DeliveryDTO.builder()
                .idDelivery(8L)
                .orderId(1L)
                .status(DeliveryStatus.EN_COURS)
                .deliveryDate(existingDelivery.getDeliveryDate())
                .build());

        DeliveryDTO result = deliveryService.getDeliveryByOrderId(1L);

        assertNotNull(result);
        verify(deliveryRepository).findByOrderIdOrder(1L);
    }

    @Test
    @DisplayName("Should throw when delivery for order ID is missing")
    void testGetDeliveryByOrderId_NotFound() {
        when(deliveryRepository.findByOrderIdOrder(2L)).thenReturn(Optional.empty());

        assertThrows(DeliveryNotFoundException.class, () -> deliveryService.getDeliveryByOrderId(2L));
    }

    @Test
    @DisplayName("Should fetch deliveries by status using pagination")
    void testGetDeliveriesByStatus() {
        Pageable pageable = PageRequest.of(0, 5);
        Delivery existingDelivery = Delivery.builder()
                .idDelivery(9L)
                .order(customerOrder)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(delivery.getDeliveryDate())
                .build();
        Page<Delivery> page = new PageImpl<>(Collections.singletonList(existingDelivery), pageable, 1);

        when(deliveryRepository.findByStatus(DeliveryStatus.PLANIFIEE, pageable)).thenReturn(page);
        when(deliveryMapper.toDTO(existingDelivery)).thenReturn(DeliveryDTO.builder()
                .idDelivery(9L)
                .orderId(1L)
                .status(DeliveryStatus.PLANIFIEE)
                .deliveryDate(existingDelivery.getDeliveryDate())
                .build());

        Page<DeliveryDTO> result = deliveryService.getDeliveriesByStatus("PLANIFIEE", pageable);

        assertEquals(1, result.getContent().size());
        verify(deliveryRepository).findByStatus(DeliveryStatus.PLANIFIEE, pageable);
    }

    @Test
    @DisplayName("Should recalculate delivery cost and persist value")
    void testCalculateDeliveryCost_RecalculatesAndPersists() {
        customerOrder.setQuantity(4);
        product.setCost(200.0);

        Delivery existingDelivery = Delivery.builder()
                .idDelivery(3L)
                .order(customerOrder)
                .cost(0.0)
                .build();

        when(deliveryRepository.findById(3L)).thenReturn(Optional.of(existingDelivery));
        when(deliveryRepository.save(existingDelivery)).thenReturn(existingDelivery);

        Double result = deliveryService.calculateDeliveryCost(3L);

        assertEquals(130.0, result, 0.001);
        assertEquals(130.0, existingDelivery.getCost(), 0.001);
        verify(deliveryRepository).save(existingDelivery);
    }

    @Test
    @DisplayName("Should throw when recalculating cost for unknown delivery")
    void testCalculateDeliveryCost_NotFound() {
        when(deliveryRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(DeliveryNotFoundException.class, () -> deliveryService.calculateDeliveryCost(4L));
    }

    @Test
    @DisplayName("Should delete delivery when it exists")
    void testDeleteDelivery_Success() {
        when(deliveryRepository.existsById(11L)).thenReturn(true);
        doNothing().when(deliveryRepository).deleteById(11L);

        deliveryService.deleteDelivery(11L);

        verify(deliveryRepository).deleteById(11L);
    }

    @Test
    @DisplayName("Should throw when deleting missing delivery")
    void testDeleteDelivery_NotFound() {
        when(deliveryRepository.existsById(12L)).thenReturn(false);

        assertThrows(DeliveryNotFoundException.class, () -> deliveryService.deleteDelivery(12L));
        verify(deliveryRepository, never()).deleteById(anyLong());
    }
}
