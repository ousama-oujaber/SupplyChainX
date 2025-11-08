package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.Supplier;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RawMaterialMapper {
    
    @Mapping(target = "supplierIds", expression = "java(mapSuppliersToIds(rawMaterial.getSuppliers()))")
    @Mapping(target = "isBelowMinimum", expression = "java(rawMaterial.isStockBelowMinimum())")
    RawMaterialDTO toDTO(RawMaterial rawMaterial);
    
    @Mapping(target = "suppliers", ignore = true)
    RawMaterial toEntity(RawMaterialDTO rawMaterialDTO);
    
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "idMaterial", ignore = true)
    void updateEntityFromDTO(RawMaterialDTO rawMaterialDTO, @MappingTarget RawMaterial rawMaterial);
    
    default Set<Long> mapSuppliersToIds(Set<Supplier> suppliers) {
        if (suppliers == null) {
            return Set.of();
        }
        return suppliers.stream()
                .map(Supplier::getIdSupplier)
                .collect(Collectors.toSet());
    }
}
