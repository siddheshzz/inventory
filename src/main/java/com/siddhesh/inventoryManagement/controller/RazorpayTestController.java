package com.siddhesh.inventoryManagement.controller;

import com.razorpay.RazorpayException;
import com.siddhesh.inventoryManagement.domain.dtos.payment.CreatePaymentResponse;
import com.siddhesh.inventoryManagement.security.CurrentUser;
import com.siddhesh.inventoryManagement.services.RazorpayPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.siddhesh.inventoryManagement.domain.dtos.payment.VerifyPaymentRequest;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class RazorpayTestController {

    private final RazorpayPaymentService paymentService;
    private final CurrentUser currentUser;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key-id}")
    private String keyId;

    @GetMapping("/config")
    public Map<String, Object> checkoutConfig() {
        boolean testMode = keyId != null && keyId.startsWith("rzp_test_");
        return Map.of("keyId", testMode ? keyId : "", "testMode", testMode);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test-order")
    public Map<String, String> createTestOrder()
            throws RazorpayException {
        String orderId = paymentService.createTestOrder();
        return Map.of("razorpayOrderId", orderId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/test-verify")
    public Map<String, Boolean> verifyTestSignature(
            @Valid @RequestBody VerifyPaymentRequest request
    ) throws RazorpayException {
        boolean matches = paymentService.verifyTestSignature(request);
        return Map.of("signatureMatches", matches);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-payment/{paymentId}")
    public Map<String, Object> fetchTestPayment(
            @PathVariable String paymentId
    ) throws RazorpayException {
        return paymentService.fetchTestPayment(paymentId);
    }

    @PostMapping("/orders/{orderId}/checkout")
    public CreatePaymentResponse createCheckout(
            @PathVariable UUID orderId
    ) throws RazorpayException {
        return paymentService.createPayment(
                orderId,
                currentUser.requireUser()
        );
    }

    @PostMapping("/orders/{orderId}/confirm")
    public Map<String, Object> confirmPayment(
            @PathVariable UUID orderId,
            @Valid @RequestBody VerifyPaymentRequest request
    ) throws RazorpayException {
        return paymentService.confirmPayment(
                orderId,
                request,
                currentUser.requireUser()
        );
    }
}
