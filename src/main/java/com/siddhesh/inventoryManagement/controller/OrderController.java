package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.entities.Order;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/orders")
public class OrderController {




//    GET    /api/v1/orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(){


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
