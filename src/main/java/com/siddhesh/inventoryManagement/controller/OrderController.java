package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderItemRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import com.siddhesh.inventoryManagement.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

//    GET    /api/v1/orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(){
        return ResponseEntity.ok(orderService.listOrders());
    }

//    POST   /api/v1/orders?userId=...
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest payload,
            @RequestParam UUID userId){
        OrderResponse created = orderService.createOrder(payload, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }



//    GET    /api/v1/orders/{id}
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

//    POST   /api/v1/orders/{id}/items
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable UUID id,
            @RequestBody CreateOrderItemRequest payload){
        OrderResponse updated = orderService.addItem(id, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

//    DELETE /api/v1/orders/{id}/items/{itemId}
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<OrderResponse> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId){
        return ResponseEntity.ok(orderService.removeItem(id, itemId));
    }

//    PATCH  /api/v1/orders/{id}/status?status=CANCELLED
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status){
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

//    GET /api/v1/profile/orders
//
//    GET    /api/v1/orders/{id}
//
//    POST   /api/v1/orders
//
//    PATCH  /api/v1/orders/{id}/status
//
//    DELETE /api/v1/orders/{id}
}
