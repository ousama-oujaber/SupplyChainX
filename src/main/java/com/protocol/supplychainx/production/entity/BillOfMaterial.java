package com.protocol.supplychainx.production.entity;

import com.protocol.supplychainx.procurement.entity.RawMaterial;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bill_of_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillOfMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bom")
    private Long idBOM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private RawMaterial material;

    @Column(nullable = false)
    private Integer quantity;

    public boolean isMaterialAvailable() {
        return material != null && material.getStock() >= quantity;
    }
}
