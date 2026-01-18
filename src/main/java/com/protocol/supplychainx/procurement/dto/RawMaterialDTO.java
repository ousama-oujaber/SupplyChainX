package com.protocol.supplychainx.procurement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialDTO {

    private Long idMaterial;

    @NotBlank(message = "Material name cannot be blank")
    @Size(min = 2, max = 100, message = "Material name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Stock cannot be null")
    @Min(value = 0, message = "Stock must be at least 0")
    private Integer stock;

    @NotNull(message = "Minimum stock cannot be null")
    @Min(value = 0, message = "Minimum stock must be at least 0")
    private Integer stockMin;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    /**
     * Supplier relationships with additional attributes (price, quantity, lead time).
     * Used for display/response. For creating/updating relationships, use the dedicated endpoints.
     */
    @Builder.Default
    private Set<SupplierMaterialDTO> suppliers = new HashSet<>();
    
    private Boolean isBelowMinimum; // Calculated field
}
