package com.protocol.supplychainx.common.exceptions.production;

public class ProductHasActiveOrdersException extends RuntimeException {
    public ProductHasActiveOrdersException(Long productId, int activeOrdersCount) {
        super("Cannot delete product with ID: " + productId + 
              ". It has " + activeOrdersCount + " active production order(s)");
    }
}
