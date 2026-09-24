package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.inventory.PurchaseRequest;
import com.siddhesh.inventoryManagement.domain.dtos.inventory.StockVerifyResponse;

import java.util.UUID;

public interface InventoryService {
    StockVerifyResponse verifyStock(UUID productId);

    StockVerifyResponse purchase(PurchaseRequest request, UUID adminUserId);

    StockVerifyResponse reconcile(UUID productId, UUID adminUserId);
}
