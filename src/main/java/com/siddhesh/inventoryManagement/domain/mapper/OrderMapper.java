package com.siddhesh.inventoryManagement.domain.mapper;

import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;
import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.dtos.orderItem.OrderItemResponse;
import com.siddhesh.inventoryManagement.domain.entities.*;
import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderMapper {


    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .quantity(product.getQuantity())
                .active(product.getActive())
                .build();

//         private OrderStatus status;

//         private BigDecimal discount;

//         private BigDecimal tax;
//         private BigDecimal shipping_charges;
//         private BigDecimal grand_total;
//         private BigDecimal subtotal;

//         private PaymentStatus paymentStatus;

//         @ManyToOne(fetch = FetchType.LAZY)
//         @JoinColumn(name = "user_id", nullable = false)
//         private User user;

//         @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
//         @Builder.Default
//         private List<OrderItem> orderItems= new ArrayList<>();

// //    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
// //    @OneToMany(mappedBy = "order")
// //    @Builder.Default
// //    private List<StockTransaction> stockTransactions = new ArrayList<>();

//         @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
//         @Builder.Default
//         private List<Payment> payments = new ArrayList<>();

//         @CreationTimestamp
//         @Column(name = "created_at", nullable = false, updatable = false)
//         private LocalDateTime createdAt;

//         @UpdateTimestamp
//         @Column(name = "updated_at")
//         private LocalDateTime updatedAt;





    }

    public Order toEntity(
            CreateOrderRequest request,
            ProductCategory category
    ) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .build();
    }
}
