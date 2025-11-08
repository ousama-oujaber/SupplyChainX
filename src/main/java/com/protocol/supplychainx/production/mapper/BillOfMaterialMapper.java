package com.protocol.supplychainx.production.mapper;

import com.protocol.supplychainx.production.dto.BillOfMaterialDTO;
import com.protocol.supplychainx.production.entity.BillOfMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillOfMaterialMapper {

    @Mapping(target = "productId", source = "product.idProduct")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "materialId", source = "material.idMaterial")
    @Mapping(target = "materialName", source = "material.name")
    @Mapping(target = "materialAvailable", expression = "java(billOfMaterial.isMaterialAvailable())")
    BillOfMaterialDTO toDTO(BillOfMaterial billOfMaterial);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "material", ignore = true)
    BillOfMaterial toEntity(BillOfMaterialDTO billOfMaterialDTO);
}
