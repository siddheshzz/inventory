package com.siddhesh.inventoryManagement.services;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.siddhesh.inventoryManagement.domain.dtos.payment.CreatePaymentResponse;
import com.siddhesh.inventoryManagement.domain.entities.*;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import com.razorpay.Utils;
import com.siddhesh.inventoryManagement.domain.dtos.payment.VerifyPaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = RazorpayException.class)
    public CreatePaymentResponse createPayment(UUID orderId, User caller)
            throws RazorpayException {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        boolean owner = order.getUser().getId().equals(caller.getId());
        boolean admin = caller.getRole() == Role.ADMIN;

        if (!owner && !admin) {
            throw new RuntimeException("Not your order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can start checkout");
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Order is not awaiting payment");
        }

        BigDecimal total = order.getGrand_total();
        long amountInPaise = total.movePointRight(2).longValueExact();

        if (amountInPaise <= 0) {
            throw new RuntimeException("Payment amount must be positive");
        }

        Payment payment = paymentRepository
                .findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(
                        orderId, PaymentStatus.PENDING
                )
                .orElse(null);

        if (payment != null) {
            if (payment.getAmount().compareTo(total) != 0) {
                throw new RuntimeException(
                        "Order total changed after checkout was created"
                );
            }

            return new CreatePaymentResponse(
                    payment.getId(),
                    order.getId(),
                    payment.getGatewayOrderId(),
                    amountInPaise,
                    payment.getCurrency()
            );
        }

        JSONObject request = new JSONObject();
        request.put("amount", amountInPaise);
        request.put("currency", "INR");
        request.put("receipt", order.getId().toString());

        com.razorpay.Order gatewayOrder =
                razorpayClient.orders.create(request);

        String gatewayOrderId = gatewayOrder.get("id");

        payment = Payment.builder()
                .order(order)
                .amount(total)
                .currency("INR")
                .gatewayOrderId(gatewayOrderId)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        return new CreatePaymentResponse(
                payment.getId(),
                order.getId(),
                gatewayOrderId,
                amountInPaise,
                "INR"
        );
    }
}