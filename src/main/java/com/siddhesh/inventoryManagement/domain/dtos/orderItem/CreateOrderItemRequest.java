package com.siddhesh.inventoryManagement.domain.dtos.orderItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderItemRequest(

            @NotNull
            UUID productId,

            @NotNull
            @Min(1)
            Integer quantity

    ) {
    }

