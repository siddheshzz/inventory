package com.siddhesh.inventoryManagement.domain.dtos.categorie;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateCategoryRequest {
    private String name;
}
