package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.Order;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import com.siddhesh.inventoryManagement.domain.mapper.OrderMapper;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.services.OrderService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    @Override
    public List<OrderResponse> listOrders() {

        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();

    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        // <[UUID,QUANTITY],][UUID,Q2]> , ORDER UUID, DISCOUNT --LETS FETCH IT FROM I THINK TABLE OR SOME SORT OF CHANGE-ABLE THING
        
        Order order = new Order()


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
