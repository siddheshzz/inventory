package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.OrderItem;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    boolean existsByProductIdAndOrderStatusIn(UUID productId, Collection<OrderStatus> statuses);
}
