package com.siddhesh.inventoryManagement.domain.dtos;

import com.siddhesh.inventoryManagement.domain.entities.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponse {
    private UUID id;

    private String name;
    private String description;
    private BigDecimal price;
    private UUID categoryId;
    private Integer quantity;
    private Boolean active;

}
