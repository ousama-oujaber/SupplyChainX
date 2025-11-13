package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerNotFoundException;
import com.protocol.supplychainx.delivery.dto.CustomerDTO;
import com.protocol.supplychainx.delivery.entity.Customer;
import com.protocol.supplychainx.delivery.mapper.CustomerMapper;
import com.protocol.supplychainx.delivery.repository.CustomerRepository;
import com.protocol.supplychainx.delivery.service.impl.CustomerService;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerDTO customerDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerDTO = CustomerDTO.builder()
                .idCustomer(1L)
                .name("ACME Corp")
                .address("123 Main St")
                .city("New York")
                .build();

        customer = Customer.builder()
                .idCustomer(1L)
                .name("ACME Corp")
                .address("123 Main St")
                .city("New York")
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void testCreateCustomer_Success() {
        // Arrange
        when(customerMapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // Act
        CustomerDTO result = customerService.createCustomer(customerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(customerDTO.getName(), result.getName());
        assertEquals(customerDTO.getAddress(), result.getAddress());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should update customer successfully")
    void testUpdateCustomer_Success() {
        // Arrange
        CustomerDTO updateDTO = CustomerDTO.builder()
                .name("ACME Corp Updated")
                .address("456 New St")
                .city("Boston")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(updateDTO);

        // Act
        CustomerDTO result = customerService.updateCustomer(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when updating non-existent customer")
    void testUpdateCustomer_NotFound() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> 
            customerService.updateCustomer(999L, customerDTO)
        );
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should get customer by ID successfully")
    void testGetCustomer_Success() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // Act
        CustomerDTO result = customerService.getCustomer(1L);

        // Assert
        assertNotNull(result);
        assertEquals(customerDTO.getName(), result.getName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when getting non-existent customer")
    void testGetCustomer_NotFound() {
        // Arrange
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> 
            customerService.getCustomer(999L)
        );
    }

    @Test
    @DisplayName("Should get all customers with pagination")
    void testGetAllCustomers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(customer);
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
        
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // Act
        Page<CustomerDTO> result = customerService.getAllCustomers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should search customers by name")
    void testSearchCustomersByName_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(customer);
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());
        
        when(customerRepository.findByNameContainingIgnoreCase("ACME", pageable)).thenReturn(customerPage);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        // Act
        Page<CustomerDTO> result = customerService.searchCustomersByName("ACME", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerRepository, times(1)).findByNameContainingIgnoreCase("ACME", pageable);
    }

    @Test
    @DisplayName("Should delete customer successfully when no active orders")
    void testDeleteCustomer_Success() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerRepository.countActiveOrdersByCustomer(eq(1L), anyList())).thenReturn(0L);
        doNothing().when(customerRepository).deleteById(1L);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).countActiveOrdersByCustomer(eq(1L), anyList());
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when deleting non-existent customer")
    void testDeleteCustomer_NotFound() {
        // Arrange
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(CustomerNotFoundException.class, () -> 
            customerService.deleteCustomer(999L)
        );
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw CustomerHasActiveOrdersException when deleting customer with active orders")
    void testDeleteCustomer_HasActiveOrders() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerRepository.countActiveOrdersByCustomer(eq(1L), anyList())).thenReturn(3L);

        // Act & Assert
        assertThrows(CustomerHasActiveOrdersException.class, () -> 
            customerService.deleteCustomer(1L)
        );
        verify(customerRepository, never()).deleteById(anyLong());
    }
}

