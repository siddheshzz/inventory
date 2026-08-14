package com.siddhesh.inventoryManagement.domain.dtos.categorie;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateCategoryRequest {
    private String name;
}
