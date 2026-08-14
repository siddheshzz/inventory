package com.siddhesh.inventoryManagement.domain.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.00")
    private BigDecimal price;

    private UUID categoryId;
}