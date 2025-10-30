package com.protocol.supplychainx.procurement.repository;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.procurement.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByName(String name);
    
    boolean existsByName(String name);
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT COUNT(o) FROM SupplyOrder o WHERE o.supplier.idSupplier = :supplierId AND o.status IN :statuses")
    long countActiveOrdersBySupplier(@Param("supplierId") Long supplierId, 
                                     @Param("statuses") List<SupplyOrderStatus> statuses);
}
