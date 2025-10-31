package com.protocol.supplychainx.production.repository;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.production.entity.ProductionOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    Page<ProductionOrder> findByStatus(ProductionOrderStatus status, Pageable pageable);
    Page<ProductionOrder> findByProductIdProduct(Long productId, Pageable pageable);
    Page<ProductionOrder> findByIsPriorityTrue(Pageable pageable);
    Page<ProductionOrder> findByStatusIn(List<ProductionOrderStatus> statuses, Pageable pageable);
    long countByProductIdProductAndStatusIn(Long productId, List<ProductionOrderStatus> statuses);
    List<ProductionOrder> findByProductIdProductAndStatus(Long productId, ProductionOrderStatus status);
}
