package com.protocol.supplychainx.production.mapper;

import com.protocol.supplychainx.production.dto.ProductDTO;
import com.protocol.supplychainx.production.entity.BillOfMaterial;
import com.protocol.supplychainx.production.entity.Product;
import com.protocol.supplychainx.production.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "billOfMaterialIds", source = "billOfMaterials", qualifiedByName = "billOfMaterialsToIds")
    @Mapping(target = "activeOrdersCount", source = "productionOrders", qualifiedByName = "countActiveOrders")
    ProductDTO toDTO(Product product);

    @Mapping(target = "billOfMaterials", ignore = true)
    @Mapping(target = "productionOrders", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @Named("billOfMaterialsToIds")
    default List<Long> billOfMaterialsToIds(List<BillOfMaterial> billOfMaterials) {
        if (billOfMaterials == null) {
            return null;
        }
        return billOfMaterials.stream()
                .map(BillOfMaterial::getIdBOM)
                .collect(Collectors.toList());
    }

    @Named("countActiveOrders")
    default Integer countActiveOrders(List<ProductionOrder> productionOrders) {
        if (productionOrders == null) {
            return 0;
        }
        return (int) productionOrders.stream()
                .filter(ProductionOrder::isActive)
                .count();
    }
}
