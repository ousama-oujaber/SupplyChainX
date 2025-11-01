package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.delivery.dto.CustomerOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomerOrderService {
    CustomerOrderDTO createCustomerOrder(CustomerOrderDTO customerOrderDTO);
    CustomerOrderDTO updateCustomerOrder(Long id, CustomerOrderDTO customerOrderDTO);
    CustomerOrderDTO getCustomerOrder(Long id);
    Page<CustomerOrderDTO> getAllCustomerOrders(Pageable pageable);
    Page<CustomerOrderDTO> getCustomerOrdersByCustomer(Long customerId, Pageable pageable);
    Page<CustomerOrderDTO> getCustomerOrdersByStatus(String status, Pageable pageable);
    void cancelCustomerOrder(Long id);
}
