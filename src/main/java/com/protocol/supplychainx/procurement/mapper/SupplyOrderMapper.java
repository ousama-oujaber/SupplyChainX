package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.SupplyOrderDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.SupplyOrder;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplyOrderMapper {
    
    @Mapping(target = "supplierId", source = "supplier.idSupplier")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "materialIds", expression = "java(mapMaterialsToIds(supplyOrder.getMaterials()))")
    SupplyOrderDTO toDTO(SupplyOrder supplyOrder);
    
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "materials", ignore = true)
    SupplyOrder toEntity(SupplyOrderDTO supplyOrderDTO);
    
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "idOrder", ignore = true)
    void updateEntityFromDTO(SupplyOrderDTO supplyOrderDTO, @MappingTarget SupplyOrder supplyOrder);
    
    default Set<Long> mapMaterialsToIds(Set<RawMaterial> materials) {
        if (materials == null) {
            return Set.of();
        }
        return materials.stream()
                .map(RawMaterial::getIdMaterial)
                .collect(Collectors.toSet());
    }
}
