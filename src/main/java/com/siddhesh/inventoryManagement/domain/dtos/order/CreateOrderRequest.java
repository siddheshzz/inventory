package com.siddhesh.inventoryManagement.domain.dtos.order;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateOrderRequest {
    private List<CreateOrderItemRequest> items;
    private UUID shippingAddressId;
    private BigDecimal discount;
}
