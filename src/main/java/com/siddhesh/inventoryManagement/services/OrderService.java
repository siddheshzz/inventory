package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderItemRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.Order;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {



//    GET /api/v1/profile/orders
    List<OrderResponse> listOrders();
//
//    GET    /api/v1/orders/{id}
    OrderResponse getOrderById(UUID id);
//
//    POST   /api/v1/order

    OrderResponse createOrder(CreateOrderRequest createOrderRequest, UUID userId);

    OrderResponse addItem(UUID orderId, CreateOrderItemRequest itemRequest);

    OrderResponse removeItem(UUID orderId, UUID itemId);





//
//    PATCH  /api/v1/orders/{id}/status
    OrderResponse updateOrderStatus(UUID id, OrderStatus status);
//
//    DELETE /api/v1/orders/{id}
    //just mark as inactive or some check without deleteing

    void deleteOrder(UUID id);
}


