package com.protocol.supplychainx.delivery.service.impl;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import com.protocol.supplychainx.common.enums.DeliveryStatus;
import com.protocol.supplychainx.common.exceptions.delivery.CustomerOrderNotFoundException;
import com.protocol.supplychainx.common.exceptions.delivery.DeliveryNotFoundException;
import com.protocol.supplychainx.delivery.dto.DeliveryDTO;
import com.protocol.supplychainx.delivery.entity.CustomerOrder;
import com.protocol.supplychainx.delivery.entity.Delivery;
import com.protocol.supplychainx.delivery.mapper.DeliveryMapper;
import com.protocol.supplychainx.delivery.repository.CustomerOrderRepository;
import com.protocol.supplychainx.delivery.repository.DeliveryRepository;
import com.protocol.supplychainx.delivery.service.IDeliveryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeliveryService implements IDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final DeliveryMapper deliveryMapper;

    @Override
    public DeliveryDTO createDelivery(DeliveryDTO deliveryDTO) {
        log.info("Creating new delivery for order ID: {}", deliveryDTO.getOrderId());

        CustomerOrder customerOrder = customerOrderRepository.findById(deliveryDTO.getOrderId())
                .orElseThrow(() -> new CustomerOrderNotFoundException(deliveryDTO.getOrderId()));

        Delivery delivery = deliveryMapper.toEntity(deliveryDTO);
        delivery.setOrder(customerOrder);
        
        if (delivery.getStatus() == null) {
            delivery.setStatus(DeliveryStatus.PLANIFIEE);
        }

        if (delivery.getCost() == null || delivery.getCost() == 0.0) {
            delivery.setCost(calculateCost(customerOrder));
        }

        if (customerOrder.getStatus() == CustomerOrderStatus.EN_PREPARATION) {
            customerOrder.setStatus(CustomerOrderStatus.EN_ROUTE);
            customerOrderRepository.save(customerOrder);
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Delivery created successfully with ID: {}", savedDelivery.getIdDelivery());

        return deliveryMapper.toDTO(savedDelivery);
    }

    @Override
    public DeliveryDTO updateDelivery(Long id, DeliveryDTO deliveryDTO) {
        log.info("Updating delivery with ID: {}", id);

        Delivery existingDelivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));

        if (deliveryDTO.getVehicle() != null) {
            existingDelivery.setVehicle(deliveryDTO.getVehicle());
        }
        
        if (deliveryDTO.getDriver() != null) {
            existingDelivery.setDriver(deliveryDTO.getDriver());
        }
        
        if (deliveryDTO.getDeliveryDate() != null) {
            existingDelivery.setDeliveryDate(deliveryDTO.getDeliveryDate());
        }
        
        if (deliveryDTO.getCost() != null) {
            existingDelivery.setCost(deliveryDTO.getCost());
        }
        
        if (deliveryDTO.getStatus() != null) {
            existingDelivery.setStatus(deliveryDTO.getStatus());
            
            if (deliveryDTO.getStatus() == DeliveryStatus.LIVREE) {
                CustomerOrder order = existingDelivery.getOrder();
                order.setStatus(CustomerOrderStatus.LIVREE);
                customerOrderRepository.save(order);
            } else if (deliveryDTO.getStatus() == DeliveryStatus.EN_COURS) {
                CustomerOrder order = existingDelivery.getOrder();
                order.setStatus(CustomerOrderStatus.EN_ROUTE);
                customerOrderRepository.save(order);
            }
        }

        Delivery updatedDelivery = deliveryRepository.save(existingDelivery);
        log.info("Delivery updated successfully with ID: {}", updatedDelivery.getIdDelivery());

        return deliveryMapper.toDTO(updatedDelivery);
    }

    @Override
    public DeliveryDTO getDelivery(Long id) {
        log.info("Fetching delivery with ID: {}", id);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));

        return deliveryMapper.toDTO(delivery);
    }

    @Override
    public DeliveryDTO getDeliveryByOrderId(Long orderId) {
        log.info("Fetching delivery for order ID: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderIdOrder(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found for order ID: " + orderId));

        return deliveryMapper.toDTO(delivery);
    }

    @Override
    public Page<DeliveryDTO> getAllDeliveries(Pageable pageable) {
        log.info("Fetching all deliveries with pagination");

        Page<Delivery> deliveries = deliveryRepository.findAll(pageable);
        return deliveries.map(deliveryMapper::toDTO);
    }

    @Override
    public Page<DeliveryDTO> getDeliveriesByStatus(String status, Pageable pageable) {
        log.info("Fetching deliveries by status: {}", status);

        DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());
        Page<Delivery> deliveries = deliveryRepository.findByStatus(deliveryStatus, pageable);
        return deliveries.map(deliveryMapper::toDTO);
    }

    @Override
    public Double calculateDeliveryCost(Long deliveryId) {
        log.info("Calculating cost for delivery ID: {}", deliveryId);

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        Double calculatedCost = calculateCost(delivery.getOrder());
        
        delivery.setCost(calculatedCost);
        deliveryRepository.save(delivery);

        log.info("Calculated cost for delivery ID {}: {}", deliveryId, calculatedCost);
        return calculatedCost;
    }

    @Override
    public void deleteDelivery(Long id) {
        log.info("Attempting to delete delivery with ID: {}", id);

        if (!deliveryRepository.existsById(id)) {
            throw new DeliveryNotFoundException(id);
        }

        deliveryRepository.deleteById(id);
        log.info("Delivery deleted successfully with ID: {}", id);
    }

    private Double calculateCost(CustomerOrder order) {
        double baseCost = 50.0;
        double productCost = order.getProduct().getCost();
        int quantity = order.getQuantity();
        
        double calculatedCost = baseCost + (productCost * quantity * 0.1);
        
        return Math.round(calculatedCost * 100.0) / 100.0;
    }
}
