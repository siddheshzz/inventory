package com.siddhesh.inventoryManagement.domain.dtos;

import com.siddhesh.inventoryManagement.domain.entities.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String phoneNumber;
    private String name;
    private String role;


}
