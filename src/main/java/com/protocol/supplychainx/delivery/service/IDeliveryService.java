package com.protocol.supplychainx.delivery.service;

import com.protocol.supplychainx.delivery.dto.DeliveryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDeliveryService {
    DeliveryDTO createDelivery(DeliveryDTO deliveryDTO);
    DeliveryDTO updateDelivery(Long id, DeliveryDTO deliveryDTO);
    DeliveryDTO getDelivery(Long id);
    DeliveryDTO getDeliveryByOrderId(Long orderId);
    Page<DeliveryDTO> getAllDeliveries(Pageable pageable);
    Page<DeliveryDTO> getDeliveriesByStatus(String status, Pageable pageable);
    Double calculateDeliveryCost(Long deliveryId);
    void deleteDelivery(Long id);
}
