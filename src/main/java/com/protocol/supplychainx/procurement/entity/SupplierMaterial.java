package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

/**
 * Pivot entity representing the relationship between Supplier and RawMaterial.
 * Contains additional attributes like unit price, minimum order quantity, lead time, and preference flag.
 */
@Entity
@Table(name = "supplier_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierMaterial {

    @EmbeddedId
    private SupplierMaterialId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supplierId")
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("materialId")
    @JoinColumn(name = "material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Column(name = "unit_price")
    @Min(value = 0, message = "Unit price must be at least 0")
    private Double unitPrice;

    @Column(name = "min_order_quantity")
    @Min(value = 1, message = "Minimum order quantity must be at least 1")
    private Integer minOrderQuantity;

    @Column(name = "lead_time_days")
    @Min(value = 0, message = "Lead time must be at least 0")
    private Integer leadTimeDays;

    @Column(name = "is_preferred")
    @Builder.Default
    private Boolean isPreferred = false;

    /**
     * Convenience constructor for creating a new supplier-material relationship.
     */
    public SupplierMaterial(Supplier supplier, RawMaterial rawMaterial) {
        this.supplier = supplier;
        this.rawMaterial = rawMaterial;
        this.id = new SupplierMaterialId(supplier.getIdSupplier(), rawMaterial.getIdMaterial());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupplierMaterial that = (SupplierMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
