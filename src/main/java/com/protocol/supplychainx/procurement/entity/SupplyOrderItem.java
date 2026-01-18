package com.protocol.supplychainx.procurement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supply_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SupplyOrder supplyOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "sub_total")
    private Double subTotal;

    /**
     * Calculate and set the subTotal based on quantity and unitPrice.
     */
    @PrePersist
    @PreUpdate
    public void calculateSubTotal() {
        if (quantity != null && unitPrice != null) {
            this.subTotal = quantity * unitPrice;
        }
    }
}

