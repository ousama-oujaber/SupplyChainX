package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_supplier")
    private Long idSupplier;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Contact information cannot be blank")
    @Size(max = 200, message = "Contact must not exceed 200 characters")
    @Column(nullable = false)
    private String contact;

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Double rating;

    @NotNull(message = "Lead time cannot be null")
    @Min(value = 1, message = "Lead time must be at least 1 day")
    @Column(nullable = false)
    private Integer leadTime;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplyOrder> orders = new ArrayList<>();

    /**
     * Bidirectional relationship to materials this supplier provides.
     * Contains additional attributes like unit price and minimum order quantity.
     */
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SupplierMaterial> suppliedMaterials = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return idSupplier != null && Objects.equals(idSupplier, supplier.idSupplier);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
