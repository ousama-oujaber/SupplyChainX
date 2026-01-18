package com.protocol.supplychainx.procurement.mapper;

import com.protocol.supplychainx.procurement.dto.SupplierMaterialDTO;
import com.protocol.supplychainx.procurement.entity.SupplierMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMaterialMapper {

    @Mapping(target = "supplierId", source = "supplier.idSupplier")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "materialId", source = "rawMaterial.idMaterial")
    @Mapping(target = "materialName", source = "rawMaterial.name")
    SupplierMaterialDTO toDTO(SupplierMaterial supplierMaterial);

    Set<SupplierMaterialDTO> toDTOSet(Set<SupplierMaterial> supplierMaterials);
}
