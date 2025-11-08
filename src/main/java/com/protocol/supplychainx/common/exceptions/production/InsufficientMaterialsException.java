package com.protocol.supplychainx.common.exceptions.production;

public class InsufficientMaterialsException extends RuntimeException {
    public InsufficientMaterialsException(String message) {
        super(message);
    }

    public InsufficientMaterialsException(Long productId, String missingMaterials) {
        super("Cannot start production for product ID: " + productId + 
              ". Insufficient materials: " + missingMaterials);
    }
}
