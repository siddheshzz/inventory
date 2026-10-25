package com.siddhesh.inventoryManagement.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method")
    private PaymentMethod paymentMethod; // e.g., "STRIPE", "PAYPAL", "COD"

    @Column(name = "transaction_reference")
    private String transactionReference; // Gateway ID (e.g., Stripe charge ID "ch_3Mxs...")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // SUCCESS, FAILED, PENDING

    @Column(name = "gateway_order_id", unique = true)
    private String gatewayOrderId;

    @Column(length = 3)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}