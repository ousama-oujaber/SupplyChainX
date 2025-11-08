package com.protocol.supplychainx.common.exceptions.production;

public class BillOfMaterialNotFoundException extends RuntimeException {
    public BillOfMaterialNotFoundException(Long id) {
        super("Bill of Material not found with ID: " + id);
    }

    public BillOfMaterialNotFoundException(String message) {
        super(message);
    }
}
