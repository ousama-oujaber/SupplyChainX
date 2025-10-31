package com.protocol.supplychainx.production.dto;

import com.protocol.supplychainx.common.enums.ProductionOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Production Order DTO for API responses and requests")
public class ProductionOrderDTO {

    @Schema(description = "Production order ID", example = "1")
    private Long idOrder;

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Product name", example = "Laptop")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity to produce", example = "50")
    private Integer quantity;

    @NotNull(message = "Status is required")
    @Schema(description = "Production order status", example = "EN_ATTENTE")
    private ProductionOrderStatus status;

    @NotNull(message = "Start date is required")
    @Schema(description = "Production start date", example = "2025-11-02")
    private LocalDate startDate;

    @Schema(description = "Production end date (estimated)", example = "2025-11-10")
    private LocalDate endDate;

    @NotNull(message = "Priority flag is required")
    @Schema(description = "Is this a priority order", example = "false")
    private Boolean isPriority;

    @Schema(description = "Estimated production time in hours")
    private Integer estimatedProductionTime;

    @Schema(description = "Are all materials available for production")
    private Boolean materialsAvailable;
}
