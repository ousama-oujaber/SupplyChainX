package com.protocol.supplychainx.procurement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrderItemDTO {

    private Long idItem;

    @NotNull(message = "Material ID cannot be null")
    private Long materialId;

    private String materialName; // For display purposes

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @PositiveOrZero(message = "Unit price must be zero or positive")
    private Double unitPrice;

    private Double subTotal; // Calculated automatically
}

