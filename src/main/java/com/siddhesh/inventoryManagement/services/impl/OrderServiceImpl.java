package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.OrderItem;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import com.siddhesh.inventoryManagement.domain.entities.Product;
import com.siddhesh.inventoryManagement.domain.entities.Role;
import com.siddhesh.inventoryManagement.domain.entities.StockTransaction;
import com.siddhesh.inventoryManagement.domain.entities.StockTransactionType;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.domain.mapper.OrderMapper;
import com.siddhesh.inventoryManagement.repositories.OrderIdempotencyRepository;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.repositories.ProductRepository;
import com.siddhesh.inventoryManagement.repositories.StockTransactionRepository;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderIdempotencyRepository orderIdempotencyRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final OrderMapper orderMapper;


    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> listOrders() {

        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();

    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponse getOrderById(UUID id, User caller) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        assertOwnerOrAdmin(order, caller);
        return orderMapper.toResponse(order);
    }

    private static void assertOwnerOrAdmin(
            com.siddhesh.inventoryManagement.domain.entities.Order order, User caller) {
        boolean owner = order.getUser().getId().equals(caller.getId());
        boolean admin = caller.getRole() == Role.ADMIN;
        if (!owner && !admin) {
            throw new RuntimeException("Not your order");
        }
    }

    private static void assertAdmin(User caller) {
        if (caller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Admin only");
        }
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, User caller) {
        return createOrder(createOrderRequest, caller, null);
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, User caller, String idempotencyKey) {
        String requestHash = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            requestHash = fingerprint(createOrderRequest, caller.getId());
            var existing = orderIdempotencyRepository.findById(idempotencyKey);
            if (existing.isPresent()) {
                if (existing.get().getExpiresAt() != null
                        && existing.get().getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
                    orderIdempotencyRepository.delete(existing.get());
                } else {
                    String storedHash = existing.get().getRequestHash();
                    if (storedHash != null && !storedHash.equals(requestHash)) {
                        throw new com.siddhesh.inventoryManagement.config.Exception.IdempotencyConflictException(idempotencyKey);
                    }
                    return orderMapper.toResponse(existing.get().getOrder());
                }
            }
        }

        if (createOrderRequest.getItems() == null || createOrderRequest.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        if (createOrderRequest.getDiscount() != null
                && createOrderRequest.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Discount cannot be negative");
        }

        User user = userRepository.findById(caller.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + caller.getId()));

        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderMapper.toEntity(createOrderRequest, user);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemReq : createOrderRequest.getItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
                throw new RuntimeException("Quantity must be at least 1");
            }

            Product product = productRepository.findByIdForUpdate(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemReq.getQuantity());

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .discount(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .lineTotal(lineTotal)
                    .build();

            order.getOrderItems().add(item);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) {
            throw new RuntimeException("Discount cannot exceed subtotal");
        }
        order.setSubtotal(subtotal);
        order.setGrand_total(subtotal.subtract(discount));

        com.siddhesh.inventoryManagement.domain.entities.Order saved =
                orderRepository.save(order);

        for (OrderItem savedItem : saved.getOrderItems()) {
            StockTransaction tx = StockTransaction.builder()
                    .product(savedItem.getProduct())
                    .order(saved)
                    .orderItem(savedItem)
                    .type(StockTransactionType.SALE)
                    .quantityChange(-savedItem.getQuantity())
                    .createdBy(user)
                    .reference("ORDER:" + saved.getId())
                    .build();
            stockTransactionRepository.save(tx);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            orderIdempotencyRepository.save(
                    com.siddhesh.inventoryManagement.domain.entities.OrderIdempotency.builder()
                            .key(idempotencyKey)
                            .order(saved)
                            .requestHash(requestHash)
                            .expiresAt(java.time.LocalDateTime.now().plusHours(24))
                            .build());
        }

        return orderMapper.toResponse(saved);
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void purgeExpiredIdempotencyKeys() {
        orderIdempotencyRepository.deleteByExpiresAtBefore(java.time.LocalDateTime.now());
    }

    private String fingerprint(CreateOrderRequest request, UUID userId) {
        String items = request.getItems().stream()
                .sorted(java.util.Comparator.comparing(
                        i -> String.valueOf(i.getProductId())))
                .map(i -> i.getProductId() + ":" + i.getQuantity())
                .collect(java.util.stream.Collectors.joining(","));
        String canonical = userId + "|" + request.getDiscount() + "|" + items;
        try {
            java.security.MessageDigest digest =
                    java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    @Transactional
    @Override
    public OrderResponse updateOrderStatus(UUID id, OrderStatus status, User caller) {
        assertAdmin(caller);
        User actor = userRepository.findById(caller.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + caller.getId()));
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (!isAllowedTransition(order.getStatus(), status)) {
            throw new RuntimeException(
                    "Illegal status transition: " + order.getStatus() + " -> " + status);
        }

        order.setStatus(status);

        if (status == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));
                product.setQuantity(product.getQuantity() + item.getQuantity());

                StockTransaction tx = StockTransaction.builder()
                        .product(product)
                        .order(order)
                        .orderItem(item)
                        .type(StockTransactionType.RETURN)
                        .quantityChange(item.getQuantity())
                        .createdBy(actor)
                        .reference("CANCEL:" + order.getId())
                        .build();
                stockTransactionRepository.save(tx);
            }
        }

        com.siddhesh.inventoryManagement.domain.entities.Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    private static boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    @Transactional
    @Override
    public OrderResponse addItem(UUID orderId, com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderItemRequest itemRequest, User caller) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        assertOwnerOrAdmin(order, caller);
        User actor = userRepository.findById(caller.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + caller.getId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be edited: " + order.getStatus());
        }

        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

        if (product.getQuantity() < itemRequest.getQuantity()) {
            throw new RuntimeException("Not enough stock for product: " + product.getName());
        }

        product.setQuantity(product.getQuantity() - itemRequest.getQuantity());

        BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .lineTotal(lineTotal)
                .build();

        // Ids present before this call, so the just-inserted line can be told
        // apart from pre-existing lines (same product may appear twice).
        java.util.Set<UUID> existingItemIds = new java.util.HashSet<>();
        for (OrderItem existing : order.getOrderItems()) {
            existingItemIds.add(existing.getId());
        }

        order.getOrderItems().add(item);

        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        order.setSubtotal(subtotal);
        order.setGrand_total(subtotal.subtract(discount));

        com.siddhesh.inventoryManagement.domain.entities.Order saved = orderRepository.save(order);
        orderRepository.flush();

        // The `item` instance above stays transient: save() on the already-managed
        // order MERGES, so persistence owns a managed copy, not our instance.
        // The SALE transaction must reference the managed copy (identified as the
        // item id that did not exist before this call).
        OrderItem managedItem = saved.getOrderItems().stream()
                .filter(i -> i.getId() != null && !existingItemIds.contains(i.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to persist order item"));

        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .order(saved)
                .orderItem(managedItem)
                .type(StockTransactionType.SALE)
                .quantityChange(-item.getQuantity())
                .createdBy(actor)
                .reference("ORDER-ADD:" + saved.getId())
                .build();
        stockTransactionRepository.save(tx);

        return orderMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public OrderResponse removeItem(UUID orderId, UUID itemId, User caller) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        assertOwnerOrAdmin(order, caller);
        User actor = userRepository.findById(caller.getId())
                .orElseThrow(() -> new RuntimeException("User not found: " + caller.getId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be edited: " + order.getStatus());
        }

        OrderItem item = order.getOrderItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not in order: " + itemId));

        if (order.getOrderItems().size() == 1) {
            throw new RuntimeException("Cannot remove last item, cancel order instead");
        }

        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct().getId()));

        // Past transactions (e.g. the SALE written when this line was added)
        // point at this row. Null the link first so the audit trail survives
        // while the line itself can be deleted (order_items_id is nullable).
        for (StockTransaction existing : stockTransactionRepository.findByOrderItem_Id(itemId)) {
            existing.setOrderItem(null);
        }

        order.getOrderItems().remove(item);

        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        order.setSubtotal(subtotal);
        order.setGrand_total(subtotal.subtract(discount).max(BigDecimal.ZERO));

        product.setQuantity(product.getQuantity() + item.getQuantity());

        com.siddhesh.inventoryManagement.domain.entities.Order saved = orderRepository.save(order);

        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .order(saved)
                .type(StockTransactionType.RETURN)
                .quantityChange(item.getQuantity())
                .createdBy(actor)
                .reference("ORDER-REMOVE:" + saved.getId() + ":" + itemId)
                .build();
        stockTransactionRepository.save(tx);

        return orderMapper.toResponse(saved);
    }

    @Override
    public void deleteOrder(UUID id) {

    }
}
