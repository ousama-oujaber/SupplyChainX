package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "raw_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long idMaterial;

    @NotBlank(message = "Material name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Stock cannot be null")
    @Min(value = 0, message = "Stock must be at least 0")
    @Column(nullable = false)
    private Integer stock;

    @NotNull(message = "Minimum stock cannot be null")
    @Min(value = 0, message = "Minimum stock must be at least 0")
    @Column(nullable = false)
    private Integer stockMin;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    @Column(nullable = false)
    private String unit;

    /**
     * Bidirectional relationship to suppliers that provide this material.
     * Contains additional attributes like unit price and minimum order quantity.
     */
    @OneToMany(mappedBy = "rawMaterial", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SupplierMaterial> supplierLinks = new HashSet<>();

    /**
     * Checks if the current stock is below the minimum required threshold.
     * @return true if stock is below minimum, false otherwise (including when values are null)
     */
    public boolean isStockBelowMinimum() {
        if (stock == null || stockMin == null) {
            return false;
        }
        return stock < stockMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawMaterial that = (RawMaterial) o;
        return idMaterial != null && Objects.equals(idMaterial, that.idMaterial);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
