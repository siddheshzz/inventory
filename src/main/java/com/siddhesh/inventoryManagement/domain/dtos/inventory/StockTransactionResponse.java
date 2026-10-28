package com.siddhesh.inventoryManagement.domain.dtos.inventory;
import com.siddhesh.inventoryManagement.domain.entities.StockTransactionType;
import java.time.LocalDateTime;
import java.util.UUID;
public record StockTransactionResponse(UUID id, UUID productId, String productName, StockTransactionType type, Integer quantityChange, String createdByName, UUID orderId, String reference, LocalDateTime createdAt) {}
