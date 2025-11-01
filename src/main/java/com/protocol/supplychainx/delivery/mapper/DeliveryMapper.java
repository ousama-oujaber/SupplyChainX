package com.protocol.supplychainx.delivery.mapper;

import com.protocol.supplychainx.delivery.dto.DeliveryDTO;
import com.protocol.supplychainx.delivery.entity.Delivery;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeliveryMapper {
    
    @Mapping(source = "order.idOrder", target = "orderId")
    @Mapping(target = "orderDetails", expression = "java(delivery.getOrder() != null && delivery.getOrder().getCustomer() != null && delivery.getOrder().getProduct() != null ? delivery.getOrder().getCustomer().getName() + \" - \" + delivery.getOrder().getProduct().getName() : \"N/A\")")
    DeliveryDTO toDTO(Delivery delivery);
    
    @Mapping(source = "orderId", target = "order.idOrder")
    @Mapping(target = "order.customer", ignore = true)
    @Mapping(target = "order.product", ignore = true)
    @Mapping(target = "order.status", ignore = true)
    @Mapping(target = "order.quantity", ignore = true)
    @Mapping(target = "order.delivery", ignore = true)
    Delivery toEntity(DeliveryDTO deliveryDTO);
    
    @Mapping(source = "orderId", target = "order.idOrder")
    @Mapping(target = "idDelivery", ignore = true)
    @Mapping(target = "order.customer", ignore = true)
    @Mapping(target = "order.product", ignore = true)
    @Mapping(target = "order.status", ignore = true)
    @Mapping(target = "order.quantity", ignore = true)
    @Mapping(target = "order.delivery", ignore = true)
    void updateEntityFromDTO(DeliveryDTO deliveryDTO, @MappingTarget Delivery delivery);
}
