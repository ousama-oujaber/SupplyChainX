package com.protocol.supplychainx.common.exceptions.delivery;

public class InsufficientProductStockException extends RuntimeException {
    
    public InsufficientProductStockException(String message) {
        super(message);
    }
    
    public InsufficientProductStockException(String productName, int available, int required) {
        super(String.format("Insufficient stock for product '%s'. Available: %d, Required: %d", 
                productName, available, required));
    }
}
