package com.protocol.supplychainx.common.exceptions.production;

public class ProductionOrderCannotBeCancelledException extends RuntimeException {
    public ProductionOrderCannotBeCancelledException(Long orderId, String status) {
        super("Cannot cancel production order with ID: " + orderId + 
              ". Order is in status: " + status + ". Only EN_ATTENTE orders can be cancelled");
    }
}
