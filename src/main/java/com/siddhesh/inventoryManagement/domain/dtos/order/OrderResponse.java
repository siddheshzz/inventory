package com.siddhesh.inventoryManagement.domain.dtos.order;

import com.siddhesh.inventoryManagement.domain.dtos.orderItem.OrderItemResponse;
import com.siddhesh.inventoryManagement.domain.entities.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shippingCharges;

    private BigDecimal grandTotal;

    private PaymentStatus paymentStatus;

    private UUID userId;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
