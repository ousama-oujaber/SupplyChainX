package com.protocol.supplychainx.procurement.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * DTO for the Supplier-Material relationship with additional attributes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierMaterialDTO {

    private Long supplierId;
    private String supplierName;      // For display purposes
    
    private Long materialId;
    private String materialName;      // For display purposes

    @Min(value = 0, message = "Unit price must be at least 0")
    private Double unitPrice;

    @Min(value = 1, message = "Minimum order quantity must be at least 1")
    private Integer minOrderQuantity;

    @Min(value = 0, message = "Lead time must be at least 0")
    private Integer leadTimeDays;

    private Boolean isPreferred;
}
