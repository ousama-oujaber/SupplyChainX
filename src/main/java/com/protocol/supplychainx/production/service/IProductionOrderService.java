package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.production.dto.ProductionOrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductionOrderService {
    
    ProductionOrderDTO createProductionOrder(ProductionOrderDTO productionOrderDTO);
    
    ProductionOrderDTO updateProductionOrder(Long id, ProductionOrderDTO productionOrderDTO);
    
    ProductionOrderDTO getProductionOrderById(Long id);
    
    Page<ProductionOrderDTO> getAllProductionOrders(Pageable pageable);
    
    Page<ProductionOrderDTO> getProductionOrdersByStatus(ProductionOrderStatus status, Pageable pageable);
    
    Page<ProductionOrderDTO> getProductionOrdersByProduct(Long productId, Pageable pageable);
    
    Page<ProductionOrderDTO> getPriorityProductionOrders(Pageable pageable);
    
    ProductionOrderDTO updateOrderStatus(Long id, ProductionOrderStatus status);
    
    void cancelProductionOrder(Long id);
    
    Integer calculateEstimatedProductionTime(Long productId, Integer quantity);
}
