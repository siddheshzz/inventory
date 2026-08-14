package com.siddhesh.inventoryManagement.domain.dtos.categorie;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryResponse {
        private UUID id;
        private String name;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;


}
