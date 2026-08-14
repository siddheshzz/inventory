package com.siddhesh.inventoryManagement.domain.dtos.order;

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

public class OrderResponse {


    private UUID id;
    private OrderStatus status;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal shipping_charges;
    private BigDecimal grand_total;

    private BigDecimal subtotal;

    private PaymentStatus paymentStatus;

    private User user;

    private List<OrderItem> orderItems= new ArrayList<>();


    private List<Payment> payments = new ArrayList<>();

    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;


}
