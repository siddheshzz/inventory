package com.siddhesh.inventoryManagement.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_idempotency_keys")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderIdempotency {

    @Id
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String key;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
