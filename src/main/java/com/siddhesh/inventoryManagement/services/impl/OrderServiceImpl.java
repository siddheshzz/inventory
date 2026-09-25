package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderRequest;
import com.siddhesh.inventoryManagement.domain.dtos.order.OrderResponse;
import com.siddhesh.inventoryManagement.domain.entities.OrderItem;
import com.siddhesh.inventoryManagement.domain.entities.OrderStatus;
import com.siddhesh.inventoryManagement.domain.entities.Product;
import com.siddhesh.inventoryManagement.domain.entities.StockTransaction;
import com.siddhesh.inventoryManagement.domain.entities.StockTransactionType;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.domain.mapper.OrderMapper;
import com.siddhesh.inventoryManagement.repositories.OrderRepository;
import com.siddhesh.inventoryManagement.repositories.ProductRepository;
import com.siddhesh.inventoryManagement.repositories.StockTransactionRepository;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
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
    public OrderResponse getOrderById(UUID id) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, UUID userId) {
        if (createOrderRequest.getItems() == null || createOrderRequest.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

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

        return orderMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public OrderResponse updateOrderStatus(UUID id, OrderStatus status) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already terminal: " + order.getStatus());
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
                        .createdBy(order.getUser())
                        .reference("CANCEL:" + order.getId())
                        .build();
                stockTransactionRepository.save(tx);
            }
        }

        com.siddhesh.inventoryManagement.domain.entities.Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public OrderResponse addItem(UUID orderId, com.siddhesh.inventoryManagement.domain.dtos.order.CreateOrderItemRequest itemRequest) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

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
                .createdBy(order.getUser())
                .reference("ORDER-ADD:" + saved.getId())
                .build();
        stockTransactionRepository.save(tx);

        return orderMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public OrderResponse removeItem(UUID orderId, UUID itemId) {
        com.siddhesh.inventoryManagement.domain.entities.Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

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
        order.setGrand_total(subtotal.subtract(discount));

        product.setQuantity(product.getQuantity() + item.getQuantity());

        com.siddhesh.inventoryManagement.domain.entities.Order saved = orderRepository.save(order);

        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .order(saved)
                .type(StockTransactionType.RETURN)
                .quantityChange(item.getQuantity())
                .createdBy(order.getUser())
                .reference("ORDER-REMOVE:" + saved.getId() + ":" + itemId)
                .build();
        stockTransactionRepository.save(tx);

        return orderMapper.toResponse(saved);
    }

    @Override
    public void deleteOrder(UUID id) {

    }
}
