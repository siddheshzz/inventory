package com.siddhesh.inventoryManagement.domain.dtos.orderItem;


import java.math.BigDecimal;
import java.util.UUID;

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