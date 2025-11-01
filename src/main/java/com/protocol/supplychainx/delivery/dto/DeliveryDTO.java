package com.protocol.supplychainx.delivery.dto;

import com.protocol.supplychainx.common.enums.DeliveryStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {

    private Long idDelivery;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    private String orderDetails;

    @Size(max = 100, message = "Vehicle must not exceed 100 characters")
    private String vehicle;

    @Size(max = 100, message = "Driver name must not exceed 100 characters")
    private String driver;

    @NotNull(message = "Delivery status cannot be null")
    private DeliveryStatus status;

    @NotNull(message = "Delivery date cannot be null")
    @FutureOrPresent(message = "Delivery date must be today or in the future")
    private LocalDate deliveryDate;

    @Min(value = 0, message = "Cost must be at least 0")
    private Double cost;
}
