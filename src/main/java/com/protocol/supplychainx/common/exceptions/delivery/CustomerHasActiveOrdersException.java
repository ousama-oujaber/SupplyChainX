package com.protocol.supplychainx.common.exceptions.delivery;

public class CustomerHasActiveOrdersException extends RuntimeException {
    
    public CustomerHasActiveOrdersException(String message) {
        super(message);
    }
    
    public CustomerHasActiveOrdersException(Long customerId, int orderCount) {
        super(String.format("Cannot delete customer with ID: %d. Customer has %d active order(s)", customerId, orderCount));
    }
}
