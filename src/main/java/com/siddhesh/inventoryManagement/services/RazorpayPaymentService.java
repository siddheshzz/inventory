package com.siddhesh.inventoryManagement.services;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.util.Map;

import com.razorpay.Utils;
import com.siddhesh.inventoryManagement.domain.dtos.payment.VerifyPaymentRequest;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final RazorpayClient razorpayClient;
    @Value("${razorpay.key-secret}")
    private String keySecret;

    public String createTestOrder() throws RazorpayException {
        JSONObject request = new JSONObject();
        request.put("amount", 10000);
        request.put("currency", "INR");

        com.razorpay.Order gatewayOrder =
                razorpayClient.orders.create(request);

        return gatewayOrder.get("id");
    }

    public boolean verifyTestSignature(VerifyPaymentRequest request)
            throws RazorpayException {

        JSONObject details = new JSONObject();
        details.put("razorpay_order_id", request.razorpayOrderId());
        details.put("razorpay_payment_id", request.razorpayPaymentId());
        details.put("razorpay_signature", request.razorpaySignature());

        return Utils.verifyPaymentSignature(details, keySecret);
    }

    public Map<String, Object> fetchTestPayment(String paymentId)
            throws RazorpayException {

        com.razorpay.Payment payment =
                razorpayClient.payments.fetch(paymentId);

        String status = payment.get("status");
        String currency = payment.get("currency");
        String orderId = payment.get("order_id");
        Number amount = payment.get("amount");

        return Map.of(
                "status", status,
                "amountInPaise", amount.longValue(),
                "currency", currency,
                "razorpayOrderId", orderId
        );
    }
}