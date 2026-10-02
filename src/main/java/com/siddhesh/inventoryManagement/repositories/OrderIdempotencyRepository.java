package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.OrderIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotency, String> {

    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime now);
}
