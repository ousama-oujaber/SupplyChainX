package com.protocol.supplychainx.procurement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    private Long idSupplier;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 2, max = 100, message = "Supplier name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Contact information cannot be blank")
    @Size(max = 200, message = "Contact must not exceed 200 characters")
    private String contact;

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Double rating;

    @NotNull(message = "Lead time cannot be null")
    @Min(value = 1, message = "Lead time must be at least 1 day")
    private Integer leadTime;

    private Integer activeOrdersCount; // Number of active orders
}
