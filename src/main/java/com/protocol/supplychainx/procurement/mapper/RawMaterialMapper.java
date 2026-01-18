package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.RawMaterialDTO;
import com.protocol.supplychainx.procurement.dto.SupplierMaterialDTO;
import com.protocol.supplychainx.procurement.entity.RawMaterial;
import com.protocol.supplychainx.procurement.entity.SupplierMaterial;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SupplierMaterialMapper.class})
public interface RawMaterialMapper {
    
    @Mapping(target = "suppliers", expression = "java(mapSupplierLinksToDTO(rawMaterial.getSupplierLinks()))")
    @Mapping(target = "isBelowMinimum", expression = "java(rawMaterial.isStockBelowMinimum())")
    RawMaterialDTO toDTO(RawMaterial rawMaterial);
    
    @Mapping(target = "supplierLinks", ignore = true)
    RawMaterial toEntity(RawMaterialDTO rawMaterialDTO);
    
    @Mapping(target = "supplierLinks", ignore = true)
    @Mapping(target = "idMaterial", ignore = true)
    void updateEntityFromDTO(RawMaterialDTO rawMaterialDTO, @MappingTarget RawMaterial rawMaterial);
    
    default Set<SupplierMaterialDTO> mapSupplierLinksToDTO(Set<SupplierMaterial> supplierLinks) {
        if (supplierLinks == null) {
            return Set.of();
        }
        return supplierLinks.stream()
                .map(link -> SupplierMaterialDTO.builder()
                        .supplierId(link.getSupplier().getIdSupplier())
                        .supplierName(link.getSupplier().getName())
                        .materialId(link.getRawMaterial().getIdMaterial())
                        .materialName(link.getRawMaterial().getName())
                        .unitPrice(link.getUnitPrice())
                        .minOrderQuantity(link.getMinOrderQuantity())
                        .leadTimeDays(link.getLeadTimeDays())
                        .isPreferred(link.getIsPreferred())
                        .build())
                .collect(Collectors.toSet());
    }
}
