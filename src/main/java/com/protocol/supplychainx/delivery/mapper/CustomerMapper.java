package com.protocol.supplychainx.delivery.mapper;

import com.protocol.supplychainx.delivery.dto.CustomerDTO;
import com.protocol.supplychainx.delivery.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {
    
    @Mapping(target = "activeOrdersCount", expression = "java(customer.getOrders() != null ? (int) customer.getOrders().stream().filter(o -> o.getStatus() != com.protocol.supplychainx.common.enums.CustomerOrderStatus.LIVREE).count() : 0)")
    CustomerDTO toDTO(Customer customer);
    
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CustomerDTO customerDTO);
    
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "idCustomer", ignore = true)
    void updateEntityFromDTO(CustomerDTO customerDTO, @MappingTarget Customer customer);
}
