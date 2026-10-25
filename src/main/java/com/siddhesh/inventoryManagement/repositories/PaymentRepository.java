package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}
