package com.siddhesh.inventoryManagement.domain.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_transactions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id")
//    private Order order;
//    @JoinColumn(name = "order_id")
    @Column
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_items_id")
    private OrderItem orderItem;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockTransactionType type;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;// e.g., -5 for SALE, +10 for PURCHASE


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Can point to an Admin/User ID who performed the transaction
//    @Column(name = "created_by", nullable = false)
//    private UUID createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(length = 255)
    private String reference; // e.g., "Supplier Invoice #123" or "Manual Audit"




}
