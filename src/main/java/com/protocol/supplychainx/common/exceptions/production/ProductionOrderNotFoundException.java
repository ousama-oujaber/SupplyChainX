package com.protocol.supplychainx.common.exceptions.production;

public class ProductionOrderNotFoundException extends RuntimeException {
    public ProductionOrderNotFoundException(Long id) {
        super("Production order not found with ID: " + id);
    }

    public ProductionOrderNotFoundException(String message) {
        super(message);
    }
}
