package com.siddhesh.inventoryManagement.domain.dtos;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserUpdateRequest {
    private String email;
    private String name;
}
