package com.protocol.supplychainx.production.mapper;

import com.protocol.supplychainx.production.dto.ProductionOrderDTO;
import com.protocol.supplychainx.production.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionOrderMapper {

    @Mapping(target = "productId", source = "product.idProduct")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "estimatedProductionTime", expression = "java(calculateEstimatedTime(productionOrder))")
    @Mapping(target = "materialsAvailable", ignore = true)
    ProductionOrderDTO toDTO(ProductionOrder productionOrder);

    @Mapping(target = "product", ignore = true)
    ProductionOrder toEntity(ProductionOrderDTO productionOrderDTO);

    default Integer calculateEstimatedTime(ProductionOrder order) {
        if (order.getProduct() == null || order.getProduct().getProductionTime() == null) {
            return null;
        }
        return order.getProduct().getProductionTime() * order.getQuantity();
    }
}
