package com.protocol.supplychainx.production.entity;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "production_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_production_order")
    private Long idOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionOrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "expected_completion_date")
    private LocalDate endDate;

    @Column(name = "is_priority", nullable = false)
    private Boolean isPriority;

    public boolean canBeCancelled() {
        return status == ProductionOrderStatus.EN_ATTENTE;
    }

    public boolean isActive() {
        return status == ProductionOrderStatus.EN_ATTENTE 
            || status == ProductionOrderStatus.EN_PRODUCTION;
    }
}
