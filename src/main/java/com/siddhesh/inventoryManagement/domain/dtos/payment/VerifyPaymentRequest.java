package com.siddhesh.inventoryManagement.domain.dtos.payment;

import jakarta.validation.constraints.NotBlank;

public record VerifyPaymentRequest(
        @NotBlank String razorpayOrderId,
        @NotBlank String razorpayPaymentId,
        @NotBlank String razorpaySignature
) {
}