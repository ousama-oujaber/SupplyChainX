package com.protocol.supplychainx.delivery.service.impl;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerNotFoundException;
import com.protocol.supplychainx.delivery.dto.CustomerDTO;
import com.protocol.supplychainx.delivery.entity.Customer;
import com.protocol.supplychainx.delivery.mapper.CustomerMapper;
import com.protocol.supplychainx.delivery.repository.CustomerRepository;
import com.protocol.supplychainx.delivery.service.ICustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        log.info("Creating new customer: {}", customerDTO.getName());

        Customer customer = customerMapper.toEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        
        log.info("Customer created successfully with ID: {}", savedCustomer.getIdCustomer());
        return customerMapper.toDTO(savedCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        log.info("Updating customer with ID: {}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setAddress(customerDTO.getAddress());
        existingCustomer.setCity(customerDTO.getCity());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getIdCustomer());

        return customerMapper.toDTO(updatedCustomer);
    }

    @Override
    public CustomerDTO getCustomer(Long id) {
        log.info("Fetching customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return customerMapper.toDTO(customer);
    }

    @Override
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.info("Fetching all customers with pagination");

        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(customerMapper::toDTO);
    }

    @Override
    public Page<CustomerDTO> searchCustomersByName(String name, Pageable pageable) {
        log.info("Searching customers by name: {}", name);

        Page<Customer> customers = customerRepository.findByNameContainingIgnoreCase(name, pageable);
        return customers.map(customerMapper::toDTO);
    }

    @Override
    public void deleteCustomer(Long id) {
        log.info("Attempting to delete customer with ID: {}", id);

        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        long activeOrdersCount = customerRepository.countActiveOrdersByCustomer(
                id, 
                Arrays.asList(CustomerOrderStatus.EN_PREPARATION, CustomerOrderStatus.EN_ROUTE)
        );

        if (activeOrdersCount > 0) {
            log.error("Cannot delete customer with ID: {}. Has {} active order(s)", id, activeOrdersCount);
            throw new CustomerHasActiveOrdersException(id, (int) activeOrdersCount);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with ID: {}", id);
    }
}
