package com.siddhesh.inventoryManagement.domain.dtos.order;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOrderRequest {

    private List<CreateOrderItemRequest> items;

    private UUID shippingAddressId;

    private BigDecimal discount;
}
