package com.protocol.supplychainx.common.exceptions.delivery;

public class DeliveryNotFoundException extends RuntimeException {
    
    public DeliveryNotFoundException(String message) {
        super(message);
    }
    
    public DeliveryNotFoundException(Long id) {
        super("Delivery not found with ID: " + id);
    }
}
