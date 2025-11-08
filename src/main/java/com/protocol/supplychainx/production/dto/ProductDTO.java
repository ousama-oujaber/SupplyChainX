package com.protocol.supplychainx.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product DTO for API responses and requests")
public class ProductDTO {

    @Schema(description = "Product ID", example = "1")
    private Long idProduct;

    @NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Laptop")
    private String name;

    @NotNull(message = "Production time is required")
    @Min(value = 1, message = "Production time must be at least 1 hour")
    @Schema(description = "Production time in hours", example = "8")
    private Integer productionTime;

    @NotNull(message = "Cost is required")
    @Min(value = 0, message = "Cost cannot be negative")
    @Schema(description = "Product cost", example = "500.00")
    private Double cost;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Product stock quantity", example = "100")
    private Integer stock;

    @Schema(description = "Bill of Materials IDs")
    private List<Long> billOfMaterialIds;

    @Schema(description = "Number of active production orders")
    private Integer activeOrdersCount;
}
