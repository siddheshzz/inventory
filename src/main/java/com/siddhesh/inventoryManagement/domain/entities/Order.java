package com.siddhesh.inventoryManagement.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Order {
//        status:enum
//        subtotal
//
//
//
//        discount
//        tax
//        shipping_charge
//        grand_total
//        payment_status
//        shipping_address_id
//        created_at
//        updated_at

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column
    private OrderStatus status;
    @Column
    private BigDecimal discount;
    @Column
    private BigDecimal tax;
    @Column
    private BigDecimal shipping_charges;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grand_total;

    @Column
    private BigDecimal subtotal;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems= new ArrayList<>();

//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//    @OneToMany(mappedBy = "order")
//    @Builder.Default
//    private List<StockTransaction> stockTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;






//    SHIPPIMG_ADDERSS
//USERID
}
