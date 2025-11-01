package com.protocol.supplychainx.delivery.dto;

import com.protocol.supplychainx.common.enums.CustomerOrderStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderDTO {

    private Long idOrder;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private String productName;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Order status cannot be null")
    private CustomerOrderStatus status;

    private DeliveryDTO delivery;
}
