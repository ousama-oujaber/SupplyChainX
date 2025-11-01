package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.delivery.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);
    CustomerDTO getCustomer(Long id);
    Page<CustomerDTO> getAllCustomers(Pageable pageable);
    Page<CustomerDTO> searchCustomersByName(String name, Pageable pageable);
    void deleteCustomer(Long id);
}
