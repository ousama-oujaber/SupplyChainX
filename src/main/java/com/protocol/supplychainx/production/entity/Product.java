package com.protocol.supplychainx.production.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer productionTime;

    @Column(nullable = false)
    private Double cost;

    @Column(nullable = false)
    private Integer stock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillOfMaterial> billOfMaterials = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionOrder> productionOrders = new ArrayList<>();

    public boolean hasActiveProductionOrders() {
        return productionOrders != null && productionOrders.stream()
                .anyMatch(order -> order.getStatus() == com.protocol.supplychainx.common.enums.ProductionOrderStatus.EN_ATTENTE
                        || order.getStatus() == com.protocol.supplychainx.common.enums.ProductionOrderStatus.EN_PRODUCTION);
    }
}
