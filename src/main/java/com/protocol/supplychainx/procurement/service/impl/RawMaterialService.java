package com.protocol.supplychainx.procurement.service.impl;

import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.dto.SupplierMaterialDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.entity.SupplierMaterial;
import com.protocol.supplychainx.procurement.entity.SupplierMaterialId;
import com.protocol.supplychainx.procurement.mapper.RawMaterialMapper;
import com.protocol.supplychainx.procurement.mapper.SupplierMaterialMapper;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.service.IRawMaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RawMaterialService implements IRawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierMaterialRepository supplierMaterialRepository;
    private final RawMaterialMapper rawMaterialMapper;
    private final SupplierMaterialMapper supplierMaterialMapper;

    @Override
    public RawMaterialDTO createRawMaterial(RawMaterialDTO rawMaterialDTO) {
        log.info("Creating new raw material: {}", rawMaterialDTO.getName());

        RawMaterial rawMaterial = rawMaterialMapper.toEntity(rawMaterialDTO);
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
    public RawMaterialDTO addSupplierToMaterial(Long materialId, SupplierMaterialDTO supplierMaterialDTO) {
        log.info("Adding supplier {} to raw material {} with attributes", 
                supplierMaterialDTO.getSupplierId(), materialId);

        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));

        Long supplierId = supplierMaterialDTO.getSupplierId();
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        // Check if relationship already exists
        if (supplierMaterialRepository.existsBySupplierIdSupplierAndRawMaterialIdMaterial(supplierId, materialId)) {
            log.warn("Supplier {} is already linked to material {}. Use update endpoint instead.", 
                    supplierId, materialId);
            return rawMaterialMapper.toDTO(material);
        }

        // Create the new relationship
        SupplierMaterial supplierMaterial = SupplierMaterial.builder()
                .id(new SupplierMaterialId(supplierId, materialId))
                .supplier(supplier)
                .rawMaterial(material)
                .unitPrice(supplierMaterialDTO.getUnitPrice())
                .minOrderQuantity(supplierMaterialDTO.getMinOrderQuantity())
                .leadTimeDays(supplierMaterialDTO.getLeadTimeDays())
                .isPreferred(supplierMaterialDTO.getIsPreferred() != null ? supplierMaterialDTO.getIsPreferred() : false)
                .build();

        material.getSupplierLinks().add(supplierMaterial);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);

        log.info("Supplier {} added successfully to material {} with pricing info", supplierId, materialId);
        return rawMaterialMapper.toDTO(updatedMaterial);
    }

    @Override
    public RawMaterialDTO updateSupplierRelationship(Long materialId, Long supplierId, 
                                                      SupplierMaterialDTO supplierMaterialDTO) {
        log.info("Updating supplier {} relationship with material {}", supplierId, materialId);

        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));

        SupplierMaterialId id = new SupplierMaterialId(supplierId, materialId);
        SupplierMaterial existingLink = supplierMaterialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Supplier %d is not linked to material %d", supplierId, materialId)));

        // Update the relationship attributes
        if (supplierMaterialDTO.getUnitPrice() != null) {
            existingLink.setUnitPrice(supplierMaterialDTO.getUnitPrice());
        }
        if (supplierMaterialDTO.getMinOrderQuantity() != null) {
            existingLink.setMinOrderQuantity(supplierMaterialDTO.getMinOrderQuantity());
        }
        if (supplierMaterialDTO.getLeadTimeDays() != null) {
            existingLink.setLeadTimeDays(supplierMaterialDTO.getLeadTimeDays());
        }
        if (supplierMaterialDTO.getIsPreferred() != null) {
            existingLink.setIsPreferred(supplierMaterialDTO.getIsPreferred());
        }

        supplierMaterialRepository.save(existingLink);
        log.info("Supplier {} relationship with material {} updated successfully", supplierId, materialId);

        // Reload material to get updated relationships
        RawMaterial updatedMaterial = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));
        return rawMaterialMapper.toDTO(updatedMaterial);
    }

    @Override
    public RawMaterialDTO removeSupplierFromMaterial(Long materialId, Long supplierId) {
        log.info("Removing supplier {} from raw material {}", supplierId, materialId);

        RawMaterial material = rawMaterialRepository.findById(materialId)
                .orElseThrow(() -> new RawMaterialNotFoundException(materialId));

        // Find and remove the supplier link
        SupplierMaterialId id = new SupplierMaterialId(supplierId, materialId);
        material.getSupplierLinks().removeIf(link -> 
                link.getId().equals(id));

        RawMaterial updatedMaterial = rawMaterialRepository.save(material);

        log.info("Supplier {} removed successfully from material {}", supplierId, materialId);
        return rawMaterialMapper.toDTO(updatedMaterial);
    }

    @Override
    public List<SupplierMaterialDTO> getSuppliersForMaterial(Long materialId) {
        log.info("Fetching all suppliers for material {}", materialId);

        if (!rawMaterialRepository.existsById(materialId)) {
            throw new RawMaterialNotFoundException(materialId);
        }

        List<SupplierMaterial> supplierLinks = supplierMaterialRepository.findAllByMaterial(materialId);
        return supplierLinks.stream()
                .map(supplierMaterialMapper::toDTO)
                .collect(Collectors.toList());
    }
}
