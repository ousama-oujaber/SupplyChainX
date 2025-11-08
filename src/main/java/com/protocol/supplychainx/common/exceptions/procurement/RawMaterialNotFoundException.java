package com.protocol.supplychainx.common.exceptions.procurement;

public class RawMaterialNotFoundException extends RuntimeException {
    
    public RawMaterialNotFoundException(String message) {
        super(message);
    }
    
    public RawMaterialNotFoundException(Long id) {
        super("Raw material not found with ID: " + id);
    }
}
