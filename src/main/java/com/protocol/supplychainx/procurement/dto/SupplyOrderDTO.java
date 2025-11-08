package com.protocol.supplychainx.procurement.dto;

import com.protocol.supplychainx.common.enums.SupplyOrderStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrderDTO {

    private Long idOrder;

    @NotNull(message = "Supplier ID cannot be null")
    private Long supplierId;

    private String supplierName; // For display purposes

    @NotNull(message = "At least one material must be selected")
    @Size(min = 1, message = "Order must contain at least one material")
    private Set<Long> materialIds;

    @NotNull(message = "Order date cannot be null")
    @PastOrPresent(message = "Order date cannot be in the future")
    private LocalDate orderDate;

    @NotNull(message = "Status cannot be null")
    private SupplyOrderStatus status;

    @Future(message = "Expected delivery date must be in the future")
    private LocalDate expectedDeliveryDate;
}
