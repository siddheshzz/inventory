package com.siddhesh.inventoryManagement.domain.dtos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AddressRequest(
        @NotBlank @Size(max = 150) String addressLine1,
        @Size(max = 150) String addressLine2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 100) String country,
        Boolean isDefault) {}
