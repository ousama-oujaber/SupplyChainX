package com.protocol.supplychainx.procurement.service.impl;

import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.mapper.RawMaterialMapper;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.service.IRawMaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RawMaterialService implements IRawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialMapper rawMaterialMapper;

    @Override
    public RawMaterialDTO createRawMaterial(RawMaterialDTO rawMaterialDTO) {
        log.info("Creating new raw material: {}", rawMaterialDTO.getName());

        RawMaterial rawMaterial = rawMaterialMapper.toEntity(rawMaterialDTO);

        if (rawMaterialDTO.getSupplierIds() != null && !rawMaterialDTO.getSupplierIds().isEmpty()) {
            Set<Supplier> suppliers = new HashSet<>();
            for (Long supplierId : rawMaterialDTO.getSupplierIds()) {
                Supplier supplier = supplierRepository.findById(supplierId)
                        .orElseThrow(() -> new SupplierNotFoundException(supplierId));
                suppliers.add(supplier);
            }
            rawMaterial.setSuppliers(suppliers);
        }

        RawMaterial savedMaterial = rawMaterialRepository.save(rawMaterial);
        log.info("Raw material created successfully with ID: {}", savedMaterial.getIdMaterial());

        return rawMaterialMapper.toDTO(savedMaterial);
    }

    @Override
    public RawMaterialDTO updateRawMaterial(Long id, RawMaterialDTO rawMaterialDTO) {
        log.info("Updating raw material with ID: {}", id);

        RawMaterial existingMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RawMaterialNotFoundException(id));

        existingMaterial.setName(rawMaterialDTO.getName());
        existingMaterial.setStock(rawMaterialDTO.getStock());
        existingMaterial.setStockMin(rawMaterialDTO.getStockMin());
        existingMaterial.setUnit(rawMaterialDTO.getUnit());

        if (rawMaterialDTO.getSupplierIds() != null) {
            Set<Supplier> suppliers = new HashSet<>();
            for (Long supplierId : rawMaterialDTO.getSupplierIds()) {
                Supplier supplier = supplierRepository.findById(supplierId)
                        .orElseThrow(() -> new SupplierNotFoundException(supplierId));
                suppliers.add(supplier);
            }
            existingMaterial.setSuppliers(suppliers);
        }

        RawMaterial updatedMaterial = rawMaterialRepository.save(existingMaterial);
        log.info("Raw material updated successfully with ID: {}", updatedMaterial.getIdMaterial());

        return rawMaterialMapper.toDTO(updatedMaterial);
    }

    @Override
    public RawMaterialDTO getRawMaterial(Long id) {
        log.info("Fetching raw material with ID: {}", id);

        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RawMaterialNotFoundException(id));

        return rawMaterialMapper.toDTO(rawMaterial);
    }

    @Override
    public Page<RawMaterialDTO> getAllRawMaterials(Pageable pageable) {
        log.info("Fetching all raw materials with pagination");

        Page<RawMaterial> materials = rawMaterialRepository.findAll(pageable);
        return materials.map(rawMaterialMapper::toDTO);
    }

    @Override
    public Page<RawMaterialDTO> searchRawMaterialsByName(String name, Pageable pageable) {
        log.info("Searching raw materials by name: {}", name);

        Page<RawMaterial> materials = rawMaterialRepository.findByNameContainingIgnoreCase(name, pageable);
        return materials.map(rawMaterialMapper::toDTO);
    }

    @Override
    public Page<RawMaterialDTO> getRawMaterialsBelowMinimumStock(Pageable pageable) {
        log.info("Fetching raw materials below minimum stock");

        Page<RawMaterial> materials = rawMaterialRepository.findByStockLessThanStockMin(pageable);
        return materials.map(rawMaterialMapper::toDTO);
    }

    @Override
    public List<RawMaterialDTO> getAllRawMaterialsBelowMinimumStock() {
        log.info("Fetching all raw materials below minimum stock");

        // Using Spring Data JPA query derivation
        List<RawMaterial> materials = rawMaterialRepository.findByStockLessThanStockMin();
        return materials.stream()
                .map(rawMaterialMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRawMaterial(Long id) {
        log.info("Attempting to delete raw material with ID: {}", id);

        if (!rawMaterialRepository.existsById(id)) {
            throw new RawMaterialNotFoundException(id);
        }

        rawMaterialRepository.deleteById(id);
        log.info("Raw material deleted successfully with ID: {}", id);
    }

    @Override
    public RawMaterialDTO addSupplierToMaterial(Long materialId, Long supplierId) {
        log.info("Adding supplier {} to raw material {}", supplierId, materialId);

        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        material.getSuppliers().add(supplier);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);

        log.info("Supplier added successfully to material");
        return rawMaterialMapper.toDTO(updatedMaterial);
    }

    @Override
    public RawMaterialDTO removeSupplierFromMaterial(Long materialId, Long supplierId) {
        log.info("Removing supplier {} from raw material {}", supplierId, materialId);

        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        material.getSuppliers().remove(supplier);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);

        log.info("Supplier removed successfully from material");
        return rawMaterialMapper.toDTO(updatedMaterial);
    }
}
