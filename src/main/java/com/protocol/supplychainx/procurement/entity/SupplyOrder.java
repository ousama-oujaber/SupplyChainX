package com.protocol.supplychainx.procurement.entity;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "supply_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order")
    private Long idOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToMany(mappedBy = "supplyOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupplyOrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplyOrderStatus status;

    private LocalDate expectedDeliveryDate;

    public boolean canBeDeleted() {
        return status == SupplyOrderStatus.EN_ATTENTE;
    }
}
