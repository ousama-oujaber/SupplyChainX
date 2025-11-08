package com.protocol.supplychainx.common.exceptions.procurement;

public class SupplyOrderCannotBeDeletedException extends RuntimeException {
    
    public SupplyOrderCannotBeDeletedException(String message) {
        super(message);
    }
    
    public SupplyOrderCannotBeDeletedException(Long id, String status) {
        super("Cannot delete supply order with ID: " + id + ". Order status is: " + status);
    }
}
