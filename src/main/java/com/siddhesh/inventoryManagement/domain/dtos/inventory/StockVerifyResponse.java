package com.siddhesh.inventoryManagement.domain.dtos.inventory;

import java.util.UUID;

public record StockVerifyResponse(
        UUID productId,
        int cachedQuantity,
        long ledgerQuantity,
        boolean match
) {
}
