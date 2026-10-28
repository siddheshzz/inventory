package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.dtos.inventory.PurchaseRequest;
import com.siddhesh.inventoryManagement.domain.dtos.inventory.StockVerifyResponse;
import com.siddhesh.inventoryManagement.domain.dtos.inventory.StockTransactionResponse;
import com.siddhesh.inventoryManagement.repositories.StockTransactionRepository;
import com.siddhesh.inventoryManagement.security.CurrentUser;
import com.siddhesh.inventoryManagement.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final CurrentUser currentUser;
    private final StockTransactionRepository stockTransactionRepository;
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ledger")
    public ResponseEntity<List<StockTransactionResponse>> ledger() { return ResponseEntity.ok(stockTransactionRepository.findAllByOrderByCreatedAtDesc().stream().map(t -> new StockTransactionResponse(t.getId(), t.getProduct().getId(), t.getProduct().getName(), t.getType(), t.getQuantityChange(), t.getCreatedBy() != null ? t.getCreatedBy().getName() : "System", t.getOrder() != null ? t.getOrder().getId() : null, t.getReference(), t.getCreatedAt())).toList()); }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/{id}/stock-verify")
    public ResponseEntity<StockVerifyResponse> verifyStock(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.verifyStock(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/purchase")
    public ResponseEntity<StockVerifyResponse> purchase(
            @RequestBody PurchaseRequest payload) {
        StockVerifyResponse result = inventoryService.purchase(payload, currentUser.requireUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/{id}/stock-reconcile")
    public ResponseEntity<StockVerifyResponse> reconcile(
            @PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.reconcile(id, currentUser.requireUser()));
    }
}
