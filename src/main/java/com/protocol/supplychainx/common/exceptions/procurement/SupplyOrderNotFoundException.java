package com.protocol.supplychainx.common.exceptions.procurement;

public class SupplyOrderNotFoundException extends RuntimeException {
    
    public SupplyOrderNotFoundException(String message) {
        super(message);
    }
    
    public SupplyOrderNotFoundException(Long id) {
        super("Supply order not found with ID: " + id);
    }
}
