package com.protocol.supplychainx.production.service;

import com.protocol.supplychainx.production.dto.BillOfMaterialDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBillOfMaterialService {
    
    BillOfMaterialDTO createBillOfMaterial(BillOfMaterialDTO billOfMaterialDTO);
    
    BillOfMaterialDTO updateBillOfMaterial(Long id, BillOfMaterialDTO billOfMaterialDTO);
    
    BillOfMaterialDTO getBillOfMaterialById(Long id);
    
    Page<BillOfMaterialDTO> getAllBillOfMaterials(Pageable pageable);
    
    List<BillOfMaterialDTO> getBillOfMaterialsByProduct(Long productId);
    
    Page<BillOfMaterialDTO> getBillOfMaterialsByProductPaginated(Long productId, Pageable pageable);
    
    void deleteBillOfMaterial(Long id);
    
    boolean checkMaterialsAvailability(Long productId, Integer quantity);
}
