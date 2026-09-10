package com.siddhesh.inventoryManagement.domain.mapper;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.dtos.orderItem.OrderItemResponse;
import com.siddhesh.inventoryManagement.domain.entities.Order;
import com.siddhesh.inventoryManagement.domain.entities.OrderItem;
import com.siddhesh.inventoryManagement.domain.entities.User;

import java.util.Collections;
import java.util.List;

public class OrderMapper {

    /**
     * Converts an Order entity into an OrderResponse DTO.
     */
    public OrderResponse toResponse(Order order) {

        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .subtotal(order.getSubtotal())
                .paymentStatus(order.getPaymentStatus())
                .userId(
                        order.getUser() != null
                                ? order.getUser().getId()
                                : null
                )
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Converts an OrderItem list into OrderItemResponse list.
     */
    private List<OrderItemResponse> toOrderItemResponses(
            List<OrderItem> orderItems
    ) {

        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        return orderItems.stream()
                .map(this::toOrderItemResponse)
                .toList();
    }

    /**
     * Converts a single OrderItem entity into an OrderItemResponse DTO.
     */
    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {

        if (orderItem == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .id(orderItem.getId())
                // Add your actual OrderItem fields here
                // .productId(orderItem.getProduct().getId())
                // .quantity(orderItem.getQuantity())
                // .price(orderItem.getPrice())
                .build();
    }

    /**
     * Converts a CreateOrderRequest into an Order entity.
     *
     * The User is passed separately because it should normally
     * come from the authenticated user/service layer rather than
     * from the client request.
     */
    public Order toEntity(
            CreateOrderRequest request,
            User user
    ) {

        if (request == null) {
            return null;
        }

        return Order.builder()
                .user(user)
                .discount(request.getDiscount())
                .tax(request.getTax())
                .shipping_charges(request.getShipping_charges())
                .subtotal(request.getSubtotal())
                .grand_total(request.getGrand_total())
                .build();
    }


}


//package com.siddhesh.inventoryManagement.domain.mapper;
//
//import com.siddhesh.inventoryManagement.domain.dtos.ProductCreationRequest;
//import com.siddhesh.inventoryManagement.domain.dtos.ProductResponse;
//import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
//import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
//import com.siddhesh.inventoryManagement.domain.dtos.orderItem.OrderItemResponse;
//import com.siddhesh.inventoryManagement.domain.entities.*;
//import jakarta.persistence.*;
//import lombok.Builder;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class OrderMapper {
//
//
//    public OrderResponse toResponse(Order order) {
//        return OrderResponse.builder()
//                .id(order.getId())
//                .status(order.getStatus())
//                .discount(order.getDiscount())
//                .tax(order.getTax())
//                .subtotal(order.getSubtotal())
//                .paymentStatus(order.getPaymentStatus())
//                .userId(
//                        order.getUser() != null
//                                ? order.getUser().getId()
//                                : null
//                )
//                .orderItems(toOrderItemResponses(order.getOrderItems()))
//                .createdAt(order.getCreatedAt())
//                .updatedAt(order.getUpdatedAt())
//                .build();
//
////         private OrderStatus status;
//
////         private BigDecimal discount;
//
////         private BigDecimal tax;
////         private BigDecimal shipping_charges;
////         private BigDecimal grand_total;
////         private BigDecimal subtotal;
//
////         private PaymentStatus paymentStatus;
//
////         @ManyToOne(fetch = FetchType.LAZY)
////         @JoinColumn(name = "user_id", nullable = false)
////         private User user;
//
////         @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
////         @Builder.Default
////         private List<OrderItem> orderItems= new ArrayList<>();
//
//// //    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//// //    @OneToMany(mappedBy = "order")
//// //    @Builder.Default
//// //    private List<StockTransaction> stockTransactions = new ArrayList<>();
//
////         @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
////         @Builder.Default
////         private List<Payment> payments = new ArrayList<>();
//
////         @CreationTimestamp
////         @Column(name = "created_at", nullable = false, updatable = false)
////         private LocalDateTime createdAt;
//
////         @UpdateTimestamp
////         @Column(name = "updated_at")
////         private LocalDateTime updatedAt;
//
//
//
//
//
//    }
//
//    public Order toEntity(
//            CreateOrderRequest request,
//            ProductCategory category
//    ) {
//        return Product.builder()
//                .name(request.getName())
//                .description(request.getDescription())
//                .price(request.getPrice())
//                .category(category)
//                .build();
//    }
//}
