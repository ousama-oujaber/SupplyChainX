package com.protocol.supplychainx.procurement.service;

import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISupplierService {
    SupplierDTO createSupplier(SupplierDTO supplierDTO);
    SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO);
    SupplierDTO getSupplier(Long id);
    Page<SupplierDTO> getAllSuppliers(Pageable pageable);
    Page<SupplierDTO> searchSuppliersByName(String name, Pageable pageable);
    void deleteSupplier(Long id);
}
