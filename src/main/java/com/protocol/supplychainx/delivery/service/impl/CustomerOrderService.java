package com.protocol.supplychainx.delivery.service.impl;

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
import com.protocol.supplychainx.delivery.service.ICustomerOrderService;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderService implements ICustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderMapper customerOrderMapper;

    @Override
    public CustomerOrderDTO createCustomerOrder(CustomerOrderDTO customerOrderDTO) {
        log.info("Creating new customer order for customer ID: {}", customerOrderDTO.getCustomerId());

        Customer customer = customerRepository.findById(customerOrderDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(customerOrderDTO.getCustomerId()));

        Product product = productRepository.findById(customerOrderDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(customerOrderDTO.getProductId()));

        if (product.getStock() < customerOrderDTO.getQuantity()) {
            log.error("Insufficient stock for product: {}. Available: {}, Required: {}", 
                    product.getName(), product.getStock(), customerOrderDTO.getQuantity());
            throw new InsufficientProductStockException(
                    product.getName(), 
                    product.getStock(), 
                    customerOrderDTO.getQuantity()
            );
        }

        CustomerOrder customerOrder = customerOrderMapper.toEntity(customerOrderDTO);
        customerOrder.setCustomer(customer);
        customerOrder.setProduct(product);
        
        if (customerOrder.getStatus() == null) {
            customerOrder.setStatus(CustomerOrderStatus.EN_PREPARATION);
        }

        product.setStock(product.getStock() - customerOrderDTO.getQuantity());
        productRepository.save(product);

        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
        log.info("Customer order created successfully with ID: {}", savedOrder.getIdOrder());

        return customerOrderMapper.toDTO(savedOrder);
    }

    @Override
    public CustomerOrderDTO updateCustomerOrder(Long id, CustomerOrderDTO customerOrderDTO) {
        log.info("Updating customer order with ID: {}", id);

        CustomerOrder existingOrder = customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));

        if (customerOrderDTO.getCustomerId() != null &&
            !customerOrderDTO.getCustomerId().equals(existingOrder.getCustomer().getIdCustomer())) {
            Customer customer = customerRepository.findById(customerOrderDTO.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException(customerOrderDTO.getCustomerId()));
            existingOrder.setCustomer(customer);
        }

        if (customerOrderDTO.getProductId() != null &&
            !customerOrderDTO.getProductId().equals(existingOrder.getProduct().getIdProduct())) {
            
            Product oldProduct = existingOrder.getProduct();
            oldProduct.setStock(oldProduct.getStock() + existingOrder.getQuantity());
            productRepository.save(oldProduct);

            Product newProduct = productRepository.findById(customerOrderDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(customerOrderDTO.getProductId()));
            
            if (newProduct.getStock() < customerOrderDTO.getQuantity()) {
                throw new InsufficientProductStockException(
                        newProduct.getName(), 
                        newProduct.getStock(), 
                        customerOrderDTO.getQuantity()
                );
            }

            newProduct.setStock(newProduct.getStock() - customerOrderDTO.getQuantity());
            productRepository.save(newProduct);
            existingOrder.setProduct(newProduct);
        }

        if (customerOrderDTO.getQuantity() != null &&
            !customerOrderDTO.getQuantity().equals(existingOrder.getQuantity())) {
            
            Product product = existingOrder.getProduct();
            int stockDifference = customerOrderDTO.getQuantity() - existingOrder.getQuantity();
            
            if (product.getStock() < stockDifference) {
                throw new InsufficientProductStockException(
                        product.getName(), 
                        product.getStock(), 
                        stockDifference
                );
            }

            product.setStock(product.getStock() - stockDifference);
            productRepository.save(product);
            existingOrder.setQuantity(customerOrderDTO.getQuantity());
        }

        if (customerOrderDTO.getStatus() != null) {
            existingOrder.setStatus(customerOrderDTO.getStatus());
        }

        CustomerOrder updatedOrder = customerOrderRepository.save(existingOrder);
        log.info("Customer order updated successfully with ID: {}", updatedOrder.getIdOrder());

        return customerOrderMapper.toDTO(updatedOrder);
    }

    @Override
    public CustomerOrderDTO getCustomerOrder(Long id) {
        log.info("Fetching customer order with ID: {}", id);

        CustomerOrder customerOrder = customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));

        return customerOrderMapper.toDTO(customerOrder);
    }

    @Override
    public Page<CustomerOrderDTO> getAllCustomerOrders(Pageable pageable) {
        log.info("Fetching all customer orders with pagination");

        Page<CustomerOrder> orders = customerOrderRepository.findAll(pageable);
        return orders.map(customerOrderMapper::toDTO);
    }

    @Override
    public Page<CustomerOrderDTO> getCustomerOrdersByCustomer(Long customerId, Pageable pageable) {
        log.info("Fetching customer orders for customer ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }

        Page<CustomerOrder> orders = customerOrderRepository.findByCustomerIdCustomer(customerId, pageable);
        return orders.map(customerOrderMapper::toDTO);
    }

    @Override
    public Page<CustomerOrderDTO> getCustomerOrdersByStatus(String status, Pageable pageable) {
        log.info("Fetching customer orders by status: {}", status);

        CustomerOrderStatus orderStatus = CustomerOrderStatus.valueOf(status.toUpperCase());
        Page<CustomerOrder> orders = customerOrderRepository.findByStatus(orderStatus, pageable);
        return orders.map(customerOrderMapper::toDTO);
    }

    @Override
    public void cancelCustomerOrder(Long id) {
        log.info("Attempting to cancel customer order with ID: {}", id);

        CustomerOrder customerOrder = customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));

        if (customerOrder.getStatus() == CustomerOrderStatus.EN_ROUTE ||
            customerOrder.getStatus() == CustomerOrderStatus.LIVREE) {
            log.error("Cannot cancel customer order with ID: {}. Status: {}", id, customerOrder.getStatus());
            throw new CustomerOrderCannotBeCancelledException(id);
        }

        Product product = customerOrder.getProduct();
        product.setStock(product.getStock() + customerOrder.getQuantity());
        productRepository.save(product);

        customerOrderRepository.deleteById(id);
        log.info("Customer order cancelled successfully with ID: {}", id);
    }
}
