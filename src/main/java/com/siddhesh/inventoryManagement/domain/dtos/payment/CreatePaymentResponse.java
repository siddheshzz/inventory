package com.siddhesh.inventoryManagement.domain.dtos.payment;

import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        UUID orderId,
        String razorpayOrderId,
        long amountInPaise,
        String currency) {
}
