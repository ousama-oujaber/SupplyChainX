package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
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
    RawMaterialDTO addSupplierToMaterial(Long materialId, Long supplierId);
    RawMaterialDTO removeSupplierFromMaterial(Long materialId, Long supplierId);
}
