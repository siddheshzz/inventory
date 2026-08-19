package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.Order;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.services.OrderService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;



    @Override
    public List<OrderResponse> listOrders() {

        List<Order> orders = orderRepository.findAll();



    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        return null;
    }

    @Override
    public OrderResponse updateOrderStatus(OrderStatus status) {
        return null;
    }

    @Override
    public void deleteOrder(UUID id) {

    }
}
