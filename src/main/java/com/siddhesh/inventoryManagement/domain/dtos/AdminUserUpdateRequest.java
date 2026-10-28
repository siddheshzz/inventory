package com.siddhesh.inventoryManagement.domain.dtos;


import com.siddhesh.inventoryManagement.domain.entities.Role;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AdminUserUpdateRequest {

    @jakarta.validation.constraints.Email
    private String email;
    private String phoneNumber;
    private String name;
    private Role role;
}
