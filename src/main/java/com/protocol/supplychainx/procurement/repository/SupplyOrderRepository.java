package com.protocol.supplychainx.procurement.repository;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.procurement.entity.SupplyOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {
    
    Page<SupplyOrder> findByStatus(SupplyOrderStatus status, Pageable pageable);
    Page<SupplyOrder> findBySupplierIdSupplier(Long supplierId, Pageable pageable);
    
    long countBySupplierIdSupplierAndStatusIn(Long supplierId, List<SupplyOrderStatus> statuses);
}
