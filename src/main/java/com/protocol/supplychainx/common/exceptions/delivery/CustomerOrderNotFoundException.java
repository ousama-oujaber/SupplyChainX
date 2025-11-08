package com.protocol.supplychainx.common.exceptions.delivery;

public class CustomerOrderNotFoundException extends RuntimeException {
    
    public CustomerOrderNotFoundException(String message) {
        super(message);
    }
    
    public CustomerOrderNotFoundException(Long id) {
        super("Customer order not found with ID: " + id);
    }
}
