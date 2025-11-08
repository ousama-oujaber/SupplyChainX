package com.protocol.supplychainx.procurement.service.impl;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import com.protocol.supplychainx.common.exceptions.procurement.RawMaterialNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplierNotFoundException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplyOrderCannotBeDeletedException;
import com.protocol.supplychainx.common.exceptions.procurement.SupplyOrderNotFoundException;
import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.entity.SupplyOrder;
import com.protocol.supplychainx.procurement.mapper.SupplyOrderMapper;
import com.protocol.supplychainx.procurement.repository.RawMaterialRepository;
import com.protocol.supplychainx.procurement.repository.SupplierRepository;
import com.protocol.supplychainx.procurement.repository.SupplyOrderRepository;
import com.protocol.supplychainx.procurement.service.ISupplyOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SupplyOrderService implements ISupplyOrderService {

    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final SupplyOrderMapper supplyOrderMapper;

    @Override
    public SupplyOrderDTO createSupplyOrder(SupplyOrderDTO supplyOrderDTO) {
        log.info("Creating new supply order for supplier ID: {}", supplyOrderDTO.getSupplierId());

        Supplier supplier = supplierRepository.findById(supplyOrderDTO.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(supplyOrderDTO.getSupplierId()));

        Set<RawMaterial> materials = new HashSet<>();
        for (Long materialId : supplyOrderDTO.getMaterialIds()) {
            RawMaterial material = rawMaterialRepository.findById(materialId)
                    .orElseThrow(() -> new RawMaterialNotFoundException(materialId));
            materials.add(material);
        }

        SupplyOrder supplyOrder = supplyOrderMapper.toEntity(supplyOrderDTO);
        supplyOrder.setSupplier(supplier);
        supplyOrder.setMaterials(materials);

        SupplyOrder savedOrder = supplyOrderRepository.save(supplyOrder);
        log.info("Supply order created successfully with ID: {}", savedOrder.getIdOrder());

        return supplyOrderMapper.toDTO(savedOrder);
    }

    @Override
    public SupplyOrderDTO updateSupplyOrder(Long id, SupplyOrderDTO supplyOrderDTO) {
        log.info("Updating supply order with ID: {}", id);

        SupplyOrder existingOrder = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new SupplyOrderNotFoundException(id));

        if (!existingOrder.getSupplier().getIdSupplier().equals(supplyOrderDTO.getSupplierId())) {
            Supplier supplier = supplierRepository.findById(supplyOrderDTO.getSupplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(supplyOrderDTO.getSupplierId()));
            existingOrder.setSupplier(supplier);
        }

        if (supplyOrderDTO.getMaterialIds() != null && !supplyOrderDTO.getMaterialIds().isEmpty()) {
            Set<RawMaterial> materials = new HashSet<>();
            for (Long materialId : supplyOrderDTO.getMaterialIds()) {
                RawMaterial material = rawMaterialRepository.findById(materialId)
                        .orElseThrow(() -> new RawMaterialNotFoundException(materialId));
                materials.add(material);
            }
            existingOrder.setMaterials(materials);
        }

        existingOrder.setOrderDate(supplyOrderDTO.getOrderDate());
        existingOrder.setStatus(supplyOrderDTO.getStatus());
        existingOrder.setExpectedDeliveryDate(supplyOrderDTO.getExpectedDeliveryDate());

        SupplyOrder updatedOrder = supplyOrderRepository.save(existingOrder);
        log.info("Supply order updated successfully with ID: {}", updatedOrder.getIdOrder());

        return supplyOrderMapper.toDTO(updatedOrder);
    }

    @Override
    public SupplyOrderDTO getSupplyOrder(Long id) {
        log.info("Fetching supply order with ID: {}", id);

        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new SupplyOrderNotFoundException(id));

        return supplyOrderMapper.toDTO(supplyOrder);
    }

    @Override
    public Page<SupplyOrderDTO> getAllSupplyOrders(Pageable pageable) {
        log.info("Fetching all supply orders with pagination");

        Page<SupplyOrder> orders = supplyOrderRepository.findAll(pageable);
        return orders.map(supplyOrderMapper::toDTO);
    }

    @Override
    public Page<SupplyOrderDTO> getSupplyOrdersByStatus(SupplyOrderStatus status, Pageable pageable) {
        log.info("Fetching supply orders by status: {}", status);

        Page<SupplyOrder> orders = supplyOrderRepository.findByStatus(status, pageable);
        return orders.map(supplyOrderMapper::toDTO);
    }

    @Override
    public Page<SupplyOrderDTO> getSupplyOrdersBySupplier(Long supplierId, Pageable pageable) {
        log.info("Fetching supply orders for supplier ID: {}", supplierId);

        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException(supplierId);
        }

        Page<SupplyOrder> orders = supplyOrderRepository.findBySupplierIdSupplier(supplierId, pageable);
        return orders.map(supplyOrderMapper::toDTO);
    }

    @Override
    public void deleteSupplyOrder(Long id) {
        log.info("Attempting to delete supply order with ID: {}", id);

        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new SupplyOrderNotFoundException(id));

        if (!supplyOrder.canBeDeleted()) {
            log.error("Cannot delete supply order with ID: {}. Status is: {}", id, supplyOrder.getStatus());
            throw new SupplyOrderCannotBeDeletedException(id, supplyOrder.getStatus().name());
        }

        supplyOrderRepository.deleteById(id);
        log.info("Supply order deleted successfully with ID: {}", id);
    }

    @Override
    public SupplyOrderDTO updateOrderStatus(Long id, SupplyOrderStatus status) {
        log.info("Updating status of supply order {} to {}", id, status);

        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new SupplyOrderNotFoundException(id));

        supplyOrder.setStatus(status);
        SupplyOrder updatedOrder = supplyOrderRepository.save(supplyOrder);

        log.info("Supply order status updated successfully");
        return supplyOrderMapper.toDTO(updatedOrder);
    }
}
