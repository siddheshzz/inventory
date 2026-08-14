package com.siddhesh.inventoryManagement.domain.dtos;


import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCreationRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private UUID categoryId;




}
