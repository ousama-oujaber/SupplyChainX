package com.protocol.supplychainx.production.repository;

import com.protocol.supplychainx.production.entity.BillOfMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {
    List<BillOfMaterial> findByProductIdProduct(Long productId);
    Page<BillOfMaterial> findByProductIdProduct(Long productId, Pageable pageable);
    List<BillOfMaterial> findByMaterialIdMaterial(Long materialId);
    boolean existsByProductIdProductAndMaterialIdMaterial(Long productId, Long materialId);
    void deleteByProductIdProduct(Long productId);
}
