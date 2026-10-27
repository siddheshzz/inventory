package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.Payment;
import com.siddhesh.inventoryManagement.domain.entities.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    Optional<Payment> findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(
            UUID orderId,
            PaymentStatus status
    );
}
