package com.protocol.supplychainx.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bill of Material DTO for API responses and requests")
public class BillOfMaterialDTO {

    @Schema(description = "BOM ID", example = "1")
    private Long idBOM;

    @NotNull(message = "Product ID is required")
    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Product name", example = "Laptop")
    private String productName;

    @NotNull(message = "Material ID is required")
    @Schema(description = "Raw material ID", example = "1")
    private Long materialId;

    @Schema(description = "Material name", example = "Steel Sheets")
    private String materialName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Required quantity of material", example = "5")
    private Integer quantity;

    @Schema(description = "Is material available in sufficient quantity")
    private Boolean materialAvailable;
}
