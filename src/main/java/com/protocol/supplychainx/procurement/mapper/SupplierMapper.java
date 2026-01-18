package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.SupplierDTO;
import com.protocol.supplychainx.procurement.dto.SupplierMaterialDTO;
import com.protocol.supplychainx.procurement.entity.Supplier;
import com.protocol.supplychainx.procurement.entity.SupplierMaterial;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SupplierMaterialMapper.class})
public interface SupplierMapper {
    
    @Mapping(target = "activeOrdersCount", expression = "java(supplier.getOrders() != null ? supplier.getOrders().size() : 0)")
    @Mapping(target = "materials", expression = "java(mapSuppliedMaterialsToDTO(supplier.getSuppliedMaterials()))")
    SupplierDTO toDTO(Supplier supplier);
    
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "suppliedMaterials", ignore = true)
    Supplier toEntity(SupplierDTO supplierDTO);
    
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "suppliedMaterials", ignore = true)
    @Mapping(target = "idSupplier", ignore = true)
    void updateEntityFromDTO(SupplierDTO supplierDTO, @MappingTarget Supplier supplier);
    
    default Set<SupplierMaterialDTO> mapSuppliedMaterialsToDTO(Set<SupplierMaterial> suppliedMaterials) {
        if (suppliedMaterials == null) {
            return Set.of();
        }
        return suppliedMaterials.stream()
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
