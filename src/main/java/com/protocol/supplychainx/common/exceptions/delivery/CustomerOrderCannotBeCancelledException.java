package com.protocol.supplychainx.common.exceptions.delivery;

public class CustomerOrderCannotBeCancelledException extends RuntimeException {
    
    public CustomerOrderCannotBeCancelledException(String message) {
        super(message);
    }
    
    public CustomerOrderCannotBeCancelledException(Long orderId) {
        super("Cannot cancel customer order with ID: " + orderId + ". Order has already been shipped or delivered");
    }
}
