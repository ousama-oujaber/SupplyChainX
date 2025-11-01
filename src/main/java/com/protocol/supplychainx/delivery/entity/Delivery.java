package com.protocol.supplychainx.delivery.entity;

import com.protocol.supplychainx.common.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_delivery")
    private Long idDelivery;

    @OneToOne
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    private String vehicle;
    private String driver;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private LocalDate deliveryDate;
    private Double cost;
}
