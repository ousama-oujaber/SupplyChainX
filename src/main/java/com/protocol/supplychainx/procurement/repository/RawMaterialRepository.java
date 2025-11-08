package com.protocol.supplychainx.procurement.repository;

import com.protocol.supplychainx.procurement.entity.RawMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    
    Optional<RawMaterial> findByName(String name);
    
    boolean existsByName(String name);
    
    Page<RawMaterial> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT r FROM RawMaterial r WHERE r.stock < r.stockMin")
    Page<RawMaterial> findByStockLessThanStockMin(Pageable pageable);
    
    @Query("SELECT r FROM RawMaterial r WHERE r.stock < r.stockMin")
    List<RawMaterial> findByStockLessThanStockMin();
}
