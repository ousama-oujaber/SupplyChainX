package com.protocol.supplychainx.common.exceptions.procurement;

public class SupplierHasActiveOrdersException extends RuntimeException {
    
    public SupplierHasActiveOrdersException(String message) {
        super(message);
    }
    
    public SupplierHasActiveOrdersException(Long id, int orderCount) {
        super("Cannot delete supplier with ID: " + id + ". Supplier has " + orderCount + " active order(s)");
    }
}
