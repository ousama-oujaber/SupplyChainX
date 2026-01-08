package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import com.protocol.supplychainx.procurement.dto.SupplyOrderItemDTO;
import com.protocol.supplychainx.procurement.entity.SupplyOrder;
import com.protocol.supplychainx.procurement.entity.SupplyOrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplyOrderMapper {
    
    @Mapping(target = "supplierId", source = "supplier.idSupplier")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "items", source = "items")
    SupplyOrderDTO toDTO(SupplyOrder supplyOrder);
    
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    SupplyOrder toEntity(SupplyOrderDTO supplyOrderDTO);
    
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "idOrder", ignore = true)
    void updateEntityFromDTO(SupplyOrderDTO supplyOrderDTO, @MappingTarget SupplyOrder supplyOrder);

    @Mapping(target = "materialId", source = "rawMaterial.idMaterial")
    @Mapping(target = "materialName", source = "rawMaterial.name")
    SupplyOrderItemDTO toItemDTO(SupplyOrderItem item);

    @Mapping(target = "supplyOrder", ignore = true)
    @Mapping(target = "rawMaterial", ignore = true)
    @Mapping(target = "idItem", ignore = true)
    SupplyOrderItem toItemEntity(SupplyOrderItemDTO itemDTO);
}
