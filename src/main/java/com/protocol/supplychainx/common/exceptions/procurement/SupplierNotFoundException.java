package com.protocol.supplychainx.common.exceptions.procurement;

public class SupplierNotFoundException extends RuntimeException {
    
    public SupplierNotFoundException(String message) {
        super(message);
    }
    
    public SupplierNotFoundException(Long id) {
        super("Supplier not found with ID: " + id);
    }
}
