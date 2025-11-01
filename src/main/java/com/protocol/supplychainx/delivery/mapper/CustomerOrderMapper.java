package com.protocol.supplychainx.delivery.mapper;

import com.protocol.supplychainx.delivery.dto.CustomerOrderDTO;
import com.protocol.supplychainx.delivery.entity.CustomerOrder;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {DeliveryMapper.class})
public interface CustomerOrderMapper {
    
    @Mapping(source = "customer.idCustomer", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "product.idProduct", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "delivery", target = "delivery")
    CustomerOrderDTO toDTO(CustomerOrder customerOrder);
    
    @Mapping(source = "customerId", target = "customer.idCustomer")
    @Mapping(source = "productId", target = "product.idProduct")
    @Mapping(target = "delivery", ignore = true)
    CustomerOrder toEntity(CustomerOrderDTO customerOrderDTO);
    
    @Mapping(source = "customerId", target = "customer.idCustomer")
    @Mapping(source = "productId", target = "product.idProduct")
    @Mapping(target = "idOrder", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    void updateEntityFromDTO(CustomerOrderDTO customerOrderDTO, @MappingTarget CustomerOrder customerOrder);
}
