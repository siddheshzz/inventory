package com.siddhesh.inventoryManagement.domain.entities;


import jakarta.persistence.*;
import jdk.jfr.Category;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private ProductCategory category;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
