package com.siddhesh.inventoryManagement.domain.dtos.inventory;

import java.util.UUID;

public record PurchaseRequest(
        UUID productId,
        Integer quantity,
        String reference
) {
}
