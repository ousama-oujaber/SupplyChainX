package com.protocol.supplychainx.procurement.service.impl;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierHasActiveOrdersException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.mapper.SupplierMapper;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.service.ISupplierService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupplierService implements ISupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        log.info("Creating new supplier: {}", supplierDTO.getName());

        Supplier supplier = supplierMapper.toEntity(supplierDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        log.info("Supplier created successfully with ID: {}", savedSupplier.getIdSupplier());
        return supplierMapper.toDTO(savedSupplier);
    }

    @Override
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        log.info("Updating supplier with ID: {}", id);

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        existingSupplier.setName(supplierDTO.getName());
        existingSupplier.setContact(supplierDTO.getContact());
        existingSupplier.setRating(supplierDTO.getRating());
        existingSupplier.setLeadTime(supplierDTO.getLeadTime());

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        log.info("Supplier updated successfully with ID: {}", updatedSupplier.getIdSupplier());

        return supplierMapper.toDTO(updatedSupplier);
    }

    @Override
    public SupplierDTO getSupplier(Long id) {
        log.info("Fetching supplier with ID: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        return supplierMapper.toDTO(supplier);
    }

    @Override
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        log.info("Fetching all suppliers with pagination");

        Page<Supplier> suppliers = supplierRepository.findAll(pageable);
        return suppliers.map(supplierMapper::toDTO);
    }

    @Override
    public Page<SupplierDTO> searchSuppliersByName(String name, Pageable pageable) {
        log.info("Searching suppliers by name: {}", name);

        Page<Supplier> suppliers = supplierRepository.findByNameContainingIgnoreCase(name, pageable);
        return suppliers.map(supplierMapper::toDTO);
    }

    @Override
    public void deleteSupplier(Long id) {
        log.info("Attempting to delete supplier with ID: {}", id);

        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException(id);
        }

        // Check if supplier has active orders
        long activeOrdersCount = supplierRepository.countActiveOrdersBySupplier(
                id, 
                Arrays.asList(SupplyOrderStatus.EN_ATTENTE, SupplyOrderStatus.EN_COURS)
        );

        if (activeOrdersCount > 0) {
            log.error("Cannot delete supplier with ID: {}. Has {} active order(s)", id, activeOrdersCount);
            throw new SupplierHasActiveOrdersException(id, (int) activeOrdersCount);
        }

        supplierRepository.deleteById(id);
        log.info("Supplier deleted successfully with ID: {}", id);
    }
}
