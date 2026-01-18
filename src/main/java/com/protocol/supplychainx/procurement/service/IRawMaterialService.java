package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.dto.SupplierMaterialDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRawMaterialService {
    RawMaterialDTO createRawMaterial(RawMaterialDTO rawMaterialDTO);
    RawMaterialDTO updateRawMaterial(Long id, RawMaterialDTO rawMaterialDTO);
    RawMaterialDTO getRawMaterial(Long id);
    Page<RawMaterialDTO> getAllRawMaterials(Pageable pageable);
    Page<RawMaterialDTO> searchRawMaterialsByName(String name, Pageable pageable);
    Page<RawMaterialDTO> getRawMaterialsBelowMinimumStock(Pageable pageable);
    List<RawMaterialDTO> getAllRawMaterialsBelowMinimumStock();
    void deleteRawMaterial(Long id);
    
    /**
     * Add a supplier to a material with relationship attributes.
     */
    RawMaterialDTO addSupplierToMaterial(Long materialId, SupplierMaterialDTO supplierMaterialDTO);
    
    /**
     * Update an existing supplier-material relationship.
     */
    RawMaterialDTO updateSupplierRelationship(Long materialId, Long supplierId, SupplierMaterialDTO supplierMaterialDTO);
    
    /**
     * Remove a supplier from a material.
     */
    RawMaterialDTO removeSupplierFromMaterial(Long materialId, Long supplierId);
    
    /**
     * Get all suppliers for a specific material.
     */
    List<SupplierMaterialDTO> getSuppliersForMaterial(Long materialId);
}
