package com.protocol.supplychainx.procurement.repository;

import com.protocol.supplychainx.procurement.entity.SupplierMaterial;
import com.protocol.supplychainx.procurement.entity.SupplierMaterialId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierMaterialRepository extends JpaRepository<SupplierMaterial, SupplierMaterialId> {

    /**
     * Find all materials supplied by a specific supplier.
     */
    List<SupplierMaterial> findBySupplierIdSupplier(Long supplierId);

    /**
     * Find all suppliers for a specific material.
     */
    List<SupplierMaterial> findByRawMaterialIdMaterial(Long materialId);

    /**
     * Find a specific supplier-material relationship.
     */
    Optional<SupplierMaterial> findBySupplierIdSupplierAndRawMaterialIdMaterial(Long supplierId, Long materialId);

    /**
     * Find the preferred supplier for a material.
     */
    @Query("SELECT sm FROM SupplierMaterial sm WHERE sm.rawMaterial.idMaterial = :materialId AND sm.isPreferred = true")
    Optional<SupplierMaterial> findPreferredSupplierForMaterial(@Param("materialId") Long materialId);

    /**
     * Find all materials by supplier ID.
     */
    @Query("SELECT sm FROM SupplierMaterial sm WHERE sm.supplier.idSupplier = :supplierId")
    List<SupplierMaterial> findAllBySupplier(@Param("supplierId") Long supplierId);

    /**
     * Find all suppliers for a material ID.
     */
    @Query("SELECT sm FROM SupplierMaterial sm WHERE sm.rawMaterial.idMaterial = :materialId")
    List<SupplierMaterial> findAllByMaterial(@Param("materialId") Long materialId);

    /**
     * Delete all supplier links for a material.
     */
    void deleteByRawMaterialIdMaterial(Long materialId);

    /**
     * Delete all material links for a supplier.
     */
    void deleteBySupplierIdSupplier(Long supplierId);

    /**
     * Check if a supplier-material relationship exists.
     */
    boolean existsBySupplierIdSupplierAndRawMaterialIdMaterial(Long supplierId, Long materialId);
}

