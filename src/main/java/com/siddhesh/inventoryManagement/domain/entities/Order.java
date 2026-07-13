package com.siddhesh.inventoryManagement.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.text.DecimalFormat;
import java.util.UUID;

@Entity
@Table(name = "order")
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

    @Column
    private OrderStatus status;
    @Column
    private DecimalFormat discount;
    @Column
    private DecimalFormat tax;
    @Column
    private DecimalFormat shipping_charges;
    @Column
    private DecimalFormat grand_total;
    @Column
    private PaymentStatus paymentStatus;



//    SHIPPIMG_ADDERSS
//USERID
}
