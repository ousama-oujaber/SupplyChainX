package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockMin;

    @Column(nullable = false)
    private String unit;

    @ManyToMany
    @JoinTable(
            name = "material_suppliers",
            joinColumns = @JoinColumn(name = "material_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    @Builder.Default
    private Set<Supplier> suppliers = new HashSet<>();

    public boolean isStockBelowMinimum() {
        return stock < stockMin;
    }
}
