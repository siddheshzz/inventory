package com.siddhesh.inventoryManagement.domain.dtos.order;


import lombok.Data;
import java.util.UUID;

@Data
public class CreateOrderItemRequest {
    private UUID productId;
    private Integer quantity;

}