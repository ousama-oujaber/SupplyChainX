package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import com.protocol.supplychainx.procurement.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {
    
    @Mapping(target = "activeOrdersCount", expression = "java(supplier.getOrders() != null ? supplier.getOrders().size() : 0)")
    SupplierDTO toDTO(Supplier supplier);
    
    @Mapping(target = "orders", ignore = true)
    Supplier toEntity(SupplierDTO supplierDTO);
    
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "idSupplier", ignore = true)
    void updateEntityFromDTO(SupplierDTO supplierDTO, @MappingTarget Supplier supplier);
}
