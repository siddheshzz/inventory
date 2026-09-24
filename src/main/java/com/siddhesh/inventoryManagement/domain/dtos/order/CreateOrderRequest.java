package com.siddhesh.inventoryManagement.domain.dtos.order;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    private List<CreateOrderItemRequest> items;
    private UUID shippingAddressId;
    private BigDecimal discount;
}
