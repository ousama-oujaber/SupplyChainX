package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerNotFoundException;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerOrderCannotBeCancelledException;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerOrderNotFoundException;
import com.protocol.supplychainx.common.exceptions.delivery.InsufficientProductStockException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.delivery.dto.CustomerOrderDTO;
import com.protocol.supplychainx.delivery.entity.Customer;
import com.protocol.supplychainx.delivery.entity.CustomerOrder;
import com.protocol.supplychainx.delivery.mapper.CustomerOrderMapper;
import com.protocol.supplychainx.delivery.repository.CustomerOrderRepository;
import com.protocol.supplychainx.delivery.repository.CustomerRepository;
import com.protocol.supplychainx.delivery.service.impl.CustomerOrderService;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.repository.ProductRepository;
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
class CustomerOrderServiceTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerOrderMapper customerOrderMapper;

    @InjectMocks
    private CustomerOrderService customerOrderService;

    private CustomerOrderDTO customerOrderDTO;
    private CustomerOrder customerOrder;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .idCustomer(1L)
                .name("ACME Corp")
                .address("123 Main St")
                .city("New York")
                .build();

        product = Product.builder()
                .idProduct(1L)
                .name("Widget A")
                .productionTime(5)
                .cost(100.0)
                .stock(50)
                .build();

        customerOrderDTO = CustomerOrderDTO.builder()
                .idOrder(1L)
                .customerId(1L)
                .productId(1L)
                .quantity(10)
                .status(CustomerOrderStatus.EN_PREPARATION)
                .build();

        customerOrder = CustomerOrder.builder()
                .idOrder(1L)
                .customer(customer)
                .product(product)
                .quantity(10)
                .status(CustomerOrderStatus.EN_PREPARATION)
                .build();
    }

    @Test
    @DisplayName("Should create customer order successfully when stock available")
    void testCreateCustomerOrder_Success() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(customerOrderMapper.toEntity(any(CustomerOrderDTO.class))).thenReturn(customerOrder);
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(customerOrder);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(customerOrderDTO);

        // Act
        CustomerOrderDTO result = customerOrderService.createCustomerOrder(customerOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(customerOrderDTO.getQuantity(), result.getQuantity());
        verify(customerOrderRepository, times(1)).save(any(CustomerOrder.class));
        verify(productRepository, times(1)).save(any(Product.class));
        verify(customerRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when creating order with invalid customer")
    void testCreateCustomerOrder_CustomerNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> 
            customerOrderService.createCustomerOrder(customerOrderDTO)
        );
        verify(customerOrderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when creating order with invalid product")
    void testCreateCustomerOrder_ProductNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> 
            customerOrderService.createCustomerOrder(customerOrderDTO)
        );
        verify(customerOrderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should throw InsufficientProductStockException when stock not available")
    void testCreateCustomerOrder_InsufficientStock() {
        // Arrange
        product.setStock(5); // Not enough for quantity 10
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InsufficientProductStockException.class, () -> 
            customerOrderService.createCustomerOrder(customerOrderDTO)
        );
        verify(customerOrderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should update customer order successfully")
    void testUpdateCustomerOrder_Success() {
        // Arrange
        CustomerOrderDTO updateDTO = CustomerOrderDTO.builder()
                .customerId(1L)
                .productId(1L)
                .quantity(10)
                .status(CustomerOrderStatus.EN_ROUTE)
                .build();

        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(customerOrder);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(updateDTO);

        // Act
        CustomerOrderDTO result = customerOrderService.updateCustomerOrder(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(customerOrderRepository, times(1)).findById(1L);
        verify(customerOrderRepository, times(1)).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should throw CustomerOrderNotFoundException when updating non-existent order")
    void testUpdateCustomerOrder_NotFound() {
        // Arrange
        when(customerOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerOrderNotFoundException.class, () -> 
            customerOrderService.updateCustomerOrder(999L, customerOrderDTO)
        );
        verify(customerOrderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should get customer order by ID successfully")
    void testGetCustomerOrder_Success() {
        // Arrange
        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(customerOrderDTO);

        // Act
        CustomerOrderDTO result = customerOrderService.getCustomerOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(customerOrderDTO.getIdOrder(), result.getIdOrder());
        verify(customerOrderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw CustomerOrderNotFoundException when getting non-existent order")
    void testGetCustomerOrder_NotFound() {
        // Arrange
        when(customerOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerOrderNotFoundException.class, () -> 
            customerOrderService.getCustomerOrder(999L)
        );
    }

    @Test
    @DisplayName("Should get all customer orders with pagination")
    void testGetAllCustomerOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CustomerOrder> orders = Arrays.asList(customerOrder);
        Page<CustomerOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(customerOrderRepository.findAll(pageable)).thenReturn(orderPage);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(customerOrderDTO);

        // Act
        Page<CustomerOrderDTO> result = customerOrderService.getAllCustomerOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerOrderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get customer orders by status")
    void testGetCustomerOrdersByStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CustomerOrder> orders = Arrays.asList(customerOrder);
        Page<CustomerOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(customerOrderRepository.findByStatus(CustomerOrderStatus.EN_PREPARATION, pageable)).thenReturn(orderPage);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(customerOrderDTO);

        // Act
        Page<CustomerOrderDTO> result = customerOrderService.getCustomerOrdersByStatus(
            "EN_PREPARATION", pageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerOrderRepository, times(1)).findByStatus(CustomerOrderStatus.EN_PREPARATION, pageable);
    }

    @Test
    @DisplayName("Should get customer orders by customer")
    void testGetCustomerOrdersByCustomer_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CustomerOrder> orders = Arrays.asList(customerOrder);
        Page<CustomerOrder> orderPage = new PageImpl<>(orders, pageable, orders.size());
        
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerOrderRepository.findByCustomerIdCustomer(1L, pageable)).thenReturn(orderPage);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(customerOrderDTO);

        // Act
        Page<CustomerOrderDTO> result = customerOrderService.getCustomerOrdersByCustomer(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerOrderRepository, times(1)).findByCustomerIdCustomer(1L, pageable);
    }

    @Test
    @DisplayName("Should throw exception when getting orders by non-existent customer")
    void testGetCustomerOrdersByCustomer_CustomerNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> 
            customerOrderService.getCustomerOrdersByCustomer(999L, pageable)
        );
    }

    @Test
    @DisplayName("Should update product and adjust stock when changing product")
    void testUpdateCustomerOrder_ChangeProductUpdatesStock() {
        Product originalProduct = customerOrder.getProduct();
        Product newProduct = Product.builder()
                .idProduct(2L)
                .name("Widget B")
                .productionTime(4)
                .cost(80.0)
                .stock(30)
                .build();

        CustomerOrderDTO updateDTO = CustomerOrderDTO.builder()
                .productId(2L)
                .quantity(10)
                .build();

        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(customerOrder);
        when(customerOrderMapper.toDTO(any(CustomerOrder.class))).thenReturn(updateDTO);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrderDTO result = customerOrderService.updateCustomerOrder(1L, updateDTO);

        assertNotNull(result);
        assertEquals(60, originalProduct.getStock());
        assertEquals(20, newProduct.getStock());
        assertEquals(newProduct, customerOrder.getProduct());
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw InsufficientProductStockException when increasing quantity beyond available stock")
    void testUpdateCustomerOrder_QuantityIncreaseInsufficientStock() {
        customerOrder.getProduct().setStock(5);

        CustomerOrderDTO updateDTO = CustomerOrderDTO.builder()
                .quantity(20)
                .build();

        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));

        assertThrows(InsufficientProductStockException.class, () ->
                customerOrderService.updateCustomerOrder(1L, updateDTO));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should cancel customer order and restore stock")
    void testCancelCustomerOrder_SuccessRestoresStock() {
        customerOrder.setStatus(CustomerOrderStatus.EN_PREPARATION);

        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(customerOrderRepository).deleteById(1L);

        customerOrderService.cancelCustomerOrder(1L);

        assertEquals(60, customerOrder.getProduct().getStock());
        verify(productRepository, times(1)).save(customerOrder.getProduct());
        verify(customerOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when cancelling order that is already in transit")
    void testCancelCustomerOrder_CannotCancelInTransit() {
        customerOrder.setStatus(CustomerOrderStatus.EN_ROUTE);

        when(customerOrderRepository.findById(1L)).thenReturn(Optional.of(customerOrder));

        assertThrows(CustomerOrderCannotBeCancelledException.class, () ->
                customerOrderService.cancelCustomerOrder(1L));
        verify(customerOrderRepository, never()).deleteById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }
}

