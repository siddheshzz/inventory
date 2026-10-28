package com.siddhesh.inventoryManagement.domain.dtos;
import java.time.LocalDateTime;
import java.util.UUID;
public record AddressResponse(UUID id, String addressLine1, String addressLine2, String city, String state, String postalCode, String country, Boolean isDefault, LocalDateTime createdAt) {}
