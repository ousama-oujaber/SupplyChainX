package com.protocol.supplychainx.production.service.impl;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import com.protocol.supplychainx.common.exceptions.production.InsufficientMaterialsException;
import com.protocol.supplychainx.common.exceptions.production.ProductNotFoundException;
import com.protocol.supplychainx.common.exceptions.production.ProductionOrderCannotBeCancelledException;
import com.protocol.supplychainx.common.exceptions.production.ProductionOrderNotFoundException;
import com.protocol.supplychainx.production.dto.ProductionOrderDTO;
import com.protocol.supplychainx.production.entity.BillOfMaterial;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.entity.ProductionOrder;
import com.protocol.supplychainx.production.mapper.ProductionOrderMapper;
import com.protocol.supplychainx.production.repository.BillOfMaterialRepository;
import com.protocol.supplychainx.production.repository.ProductRepository;
import com.protocol.supplychainx.production.repository.ProductionOrderRepository;
import com.protocol.supplychainx.production.service.IBillOfMaterialService;
import com.protocol.supplychainx.production.service.IProductionOrderService;
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
public class ProductionOrderService implements IProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductionOrderMapper productionOrderMapper;
    private final IBillOfMaterialService billOfMaterialService;

    @Override
    public ProductionOrderDTO createProductionOrder(ProductionOrderDTO productionOrderDTO) {
        log.info("Creating new production order for product ID: {}", productionOrderDTO.getProductId());

        Product product = productRepository.findById(productionOrderDTO.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(productionOrderDTO.getProductId()));

        boolean materialsAvailable = billOfMaterialService.checkMaterialsAvailability(
                productionOrderDTO.getProductId(),
                productionOrderDTO.getQuantity()
        );

        if (!materialsAvailable) {
            String missingMaterials = getMissingMaterialsDetails(productionOrderDTO.getProductId(), productionOrderDTO.getQuantity());
            throw new InsufficientMaterialsException(productionOrderDTO.getProductId(), missingMaterials);
        }

        ProductionOrder productionOrder = productionOrderMapper.toEntity(productionOrderDTO);
        productionOrder.setProduct(product);

        if (productionOrder.getIsPriority() == null) {
            productionOrder.setIsPriority(false);
        }

        ProductionOrder savedOrder = productionOrderRepository.save(productionOrder);

        log.info("Production order created successfully with ID: {}", savedOrder.getIdOrder());
        ProductionOrderDTO resultDTO = productionOrderMapper.toDTO(savedOrder);
        resultDTO.setMaterialsAvailable(true);
        return resultDTO;
    }

    @Override
    public ProductionOrderDTO updateProductionOrder(Long id, ProductionOrderDTO productionOrderDTO) {
        log.info("Updating production order with ID: {}", id);

        ProductionOrder existingOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ProductionOrderNotFoundException(id));

        existingOrder.setQuantity(productionOrderDTO.getQuantity());
        existingOrder.setStatus(productionOrderDTO.getStatus());
        existingOrder.setStartDate(productionOrderDTO.getStartDate());
        existingOrder.setEndDate(productionOrderDTO.getEndDate());
        existingOrder.setIsPriority(productionOrderDTO.getIsPriority());

        ProductionOrder updatedOrder = productionOrderRepository.save(existingOrder);

        log.info("Production order updated successfully with ID: {}", id);
        return productionOrderMapper.toDTO(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionOrderDTO getProductionOrderById(Long id) {
        log.info("Fetching production order with ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ProductionOrderNotFoundException(id));

        ProductionOrderDTO dto = productionOrderMapper.toDTO(productionOrder);
        
        boolean materialsAvailable = billOfMaterialService.checkMaterialsAvailability(
                productionOrder.getProduct().getIdProduct(),
                productionOrder.getQuantity()
        );
        dto.setMaterialsAvailable(materialsAvailable);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getAllProductionOrders(Pageable pageable) {
        log.info("Fetching all production orders with pagination");

        Page<ProductionOrder> orders = productionOrderRepository.findAll(pageable);
        return orders.map(productionOrderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getProductionOrdersByStatus(ProductionOrderStatus status, Pageable pageable) {
        log.info("Fetching production orders by status: {}", status);

        Page<ProductionOrder> orders = productionOrderRepository.findByStatus(status, pageable);
        return orders.map(productionOrderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getProductionOrdersByProduct(Long productId, Pageable pageable) {
        log.info("Fetching production orders for product ID: {}", productId);

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        Page<ProductionOrder> orders = productionOrderRepository.findByProductIdProduct(productId, pageable);
        return orders.map(productionOrderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionOrderDTO> getPriorityProductionOrders(Pageable pageable) {
        log.info("Fetching priority production orders");

        Page<ProductionOrder> orders = productionOrderRepository.findByIsPriorityTrue(pageable);
        return orders.map(productionOrderMapper::toDTO);
    }

    @Override
    public ProductionOrderDTO updateOrderStatus(Long id, ProductionOrderStatus status) {
        log.info("Updating production order status for ID: {} to: {}", id, status);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ProductionOrderNotFoundException(id));

        productionOrder.setStatus(status);
        ProductionOrder updatedOrder = productionOrderRepository.save(productionOrder);

        log.info("Production order status updated successfully for ID: {}", id);
        return productionOrderMapper.toDTO(updatedOrder);
    }

    @Override
    public void cancelProductionOrder(Long id) {
        log.info("Attempting to cancel production order with ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ProductionOrderNotFoundException(id));

        if (!productionOrder.canBeCancelled()) {
            log.error("Cannot cancel production order with ID: {}. Current status: {}", id, productionOrder.getStatus());
            throw new ProductionOrderCannotBeCancelledException(id, productionOrder.getStatus().toString());
        }

        productionOrderRepository.deleteById(id);
        log.info("Production order cancelled successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateEstimatedProductionTime(Long productId, Integer quantity) {
        log.info("Calculating estimated production time for product ID: {} with quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getProductionTime() == null) {
            log.warn("Product ID: {} does not have production time configured", productId);
            return null;
        }

        int estimatedTime = product.getProductionTime() * quantity;
        log.info("Estimated production time: {} hours", estimatedTime);
        return estimatedTime;
    }

    private String getMissingMaterialsDetails(Long productId, Integer quantity) {
        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductIdProduct(productId);

        return billOfMaterials.stream()
                .filter(bom -> {
                    int required = bom.getQuantity() * quantity;
                    int available = bom.getMaterial().getStock();
                    return available < required;
                })
                .map(bom -> String.format("%s (Required: %d, Available: %d)",
                        bom.getMaterial().getName(),
                        bom.getQuantity() * quantity,
                        bom.getMaterial().getStock()))
                .collect(Collectors.joining(", "));
    }
}
