package com.protocol.supplychainx.common.exceptions.delivery;

public class CustomerNotFoundException extends RuntimeException {
    
    public CustomerNotFoundException(String message) {
        super(message);
    }
    
    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
    }
}
