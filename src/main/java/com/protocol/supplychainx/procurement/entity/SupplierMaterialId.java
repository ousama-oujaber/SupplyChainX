package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for the SupplierMaterial pivot entity.
 * Combines supplierId and materialId to form a unique identifier.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierMaterialId implements Serializable {

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "material_id")
    private Long materialId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierMaterialId that = (SupplierMaterialId) o;
        return Objects.equals(supplierId, that.supplierId) &&
               Objects.equals(materialId, that.materialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierId, materialId);
    }
}
