package com.siddhesh.inventoryManagement.domain.dtos.orderItem;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {

    private UUID id;

    private UUID productId;

    private String productName;

    private String productSku;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal lineTotal;
}