package com.protocol.supplychainx.production.service.impl;

import com.protocol.supplychainx.common.exceptions.production.BillOfMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.production.dto.BillOfMaterialDTO;
import com.protocol.supplychainx.production.entity.BillOfMaterial;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.mapper.BillOfMaterialMapper;
import com.protocol.supplychainx.production.repository.BillOfMaterialRepository;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.production.service.IBillOfMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillOfMaterialService implements IBillOfMaterialService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final BillOfMaterialMapper billOfMaterialMapper;

    @Override
    public BillOfMaterialDTO createBillOfMaterial(BillOfMaterialDTO billOfMaterialDTO) {
        log.info("Creating new Bill of Material for product ID: {} and material ID: {}",
                billOfMaterialDTO.getProductId(), billOfMaterialDTO.getMaterialId());

        Product product = productRepository.findById(billOfMaterialDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(billOfMaterialDTO.getProductId()));

        RawMaterial material = rawMaterialRepository.findById(billOfMaterialDTO.getMaterialId())
                .orElseThrow(() -> new RawMaterialNotFoundException(billOfMaterialDTO.getMaterialId()));

        BillOfMaterial billOfMaterial = billOfMaterialMapper.toEntity(billOfMaterialDTO);
        billOfMaterial.setProduct(product);
        billOfMaterial.setMaterial(material);

        BillOfMaterial savedBOM = billOfMaterialRepository.save(billOfMaterial);

        log.info("Bill of Material created successfully with ID: {}", savedBOM.getIdBOM());
        return billOfMaterialMapper.toDTO(savedBOM);
    }

    @Override
    public BillOfMaterialDTO updateBillOfMaterial(Long id, BillOfMaterialDTO billOfMaterialDTO) {
        log.info("Updating Bill of Material with ID: {}", id);

        BillOfMaterial existingBOM = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new BillOfMaterialNotFoundException(id));

        existingBOM.setQuantity(billOfMaterialDTO.getQuantity());

        BillOfMaterial updatedBOM = billOfMaterialRepository.save(existingBOM);

        log.info("Bill of Material updated successfully with ID: {}", id);
        return billOfMaterialMapper.toDTO(updatedBOM);
    }

    @Override
    @Transactional(readOnly = true)
    public BillOfMaterialDTO getBillOfMaterialById(Long id) {
        log.info("Fetching Bill of Material with ID: {}", id);

        BillOfMaterial billOfMaterial = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new BillOfMaterialNotFoundException(id));

        return billOfMaterialMapper.toDTO(billOfMaterial);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillOfMaterialDTO> getAllBillOfMaterials(Pageable pageable) {
        log.info("Fetching all Bills of Material with pagination");

        Page<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findAll(pageable);
        return billOfMaterials.map(billOfMaterialMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillOfMaterialDTO> getBillOfMaterialsByProduct(Long productId) {
        log.info("Fetching Bills of Material for product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductIdProduct(productId);
        return billOfMaterials.stream()
                .map(billOfMaterialMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillOfMaterialDTO> getBillOfMaterialsByProductPaginated(Long productId, Pageable pageable) {
        log.info("Fetching Bills of Material for product ID: {} with pagination", productId);

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        Page<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductIdProduct(productId, pageable);
        return billOfMaterials.map(billOfMaterialMapper::toDTO);
    }

    @Override
    public void deleteBillOfMaterial(Long id) {
        log.info("Deleting Bill of Material with ID: {}", id);

        if (!billOfMaterialRepository.existsById(id)) {
            throw new BillOfMaterialNotFoundException(id);
        }

        billOfMaterialRepository.deleteById(id);
        log.info("Bill of Material deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkMaterialsAvailability(Long productId, Integer quantity) {
        log.info("Checking materials availability for product ID: {} with quantity: {}", productId, quantity);

        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductIdProduct(productId);

        if (billOfMaterials.isEmpty()) {
            log.warn("No Bill of Materials found for product ID: {}", productId);
            return false;
        }

        for (BillOfMaterial bom : billOfMaterials) {
            int requiredQuantity = bom.getQuantity() * quantity;
            int availableStock = bom.getMaterial().getStock();

            if (availableStock < requiredQuantity) {
                log.warn("Insufficient material {} (ID: {}). Required: {}, Available: {}",
                        bom.getMaterial().getName(), bom.getMaterial().getIdMaterial(),
                        requiredQuantity, availableStock);
                return false;
            }
        }

        log.info("All materials are available for production");
        return true;
    }
}
