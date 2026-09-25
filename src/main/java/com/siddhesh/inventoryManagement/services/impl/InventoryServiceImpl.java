package com.siddhesh.inventoryManagement.services.impl;

import com.siddhesh.inventoryManagement.domain.dtos.inventory.PurchaseRequest;
import com.siddhesh.inventoryManagement.domain.dtos.inventory.StockVerifyResponse;
import com.siddhesh.inventoryManagement.domain.entities.Product;
import com.siddhesh.inventoryManagement.domain.entities.StockTransaction;
import com.siddhesh.inventoryManagement.domain.entities.StockTransactionType;
import com.siddhesh.inventoryManagement.domain.entities.User;
import com.siddhesh.inventoryManagement.repositories.ProductRepository;
import com.siddhesh.inventoryManagement.repositories.StockTransactionRepository;
import com.siddhesh.inventoryManagement.repositories.UserRepository;
import com.siddhesh.inventoryManagement.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public StockVerifyResponse verifyStock(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        long ledger = stockTransactionRepository.sumQuantityChangeByProductId(productId);

        return new StockVerifyResponse(
                productId,
                product.getQuantity(),
                ledger,
                product.getQuantity() == ledger
        );
    }

    @Transactional
    @Override
    public StockVerifyResponse purchase(PurchaseRequest request, UUID adminUserId) {
        if (request.quantity() == null || request.quantity() < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        Product product = productRepository.findByIdForUpdate(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + request.productId()));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + adminUserId));

        product.setQuantity(product.getQuantity() + request.quantity());

        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .type(StockTransactionType.PURCHASE)
                .quantityChange(request.quantity())
                .createdBy(admin)
                .reference(request.reference() != null ? request.reference() : "PURCHASE:" + product.getId())
                .build();
        stockTransactionRepository.save(tx);

        long ledger = stockTransactionRepository.sumQuantityChangeByProductId(product.getId());

        return new StockVerifyResponse(
                product.getId(),
                product.getQuantity(),
                ledger,
                product.getQuantity() == ledger
        );
    }

    @Transactional
    @Override
    public StockVerifyResponse reconcile(UUID productId, UUID adminUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + adminUserId));

        long ledger = stockTransactionRepository.sumQuantityChangeByProductId(productId);
        long diff = (long) product.getQuantity() - ledger;

        if (diff != 0) {
            StockTransaction tx = StockTransaction.builder()
                    .product(product)
                    .type(StockTransactionType.ADJUSTMENT)
                    .quantityChange((int) diff)
                    .createdBy(admin)
                    .reference("RECONCILE:" + product.getId())
                    .build();
            stockTransactionRepository.save(tx);
            ledger = stockTransactionRepository.sumQuantityChangeByProductId(productId);
        }

        return new StockVerifyResponse(
                productId,
                product.getQuantity(),
                ledger,
                product.getQuantity() == ledger
        );
    }
}
