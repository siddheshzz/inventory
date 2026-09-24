package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.dtos.inventory.PurchaseRequest;
import com.siddhesh.inventoryManagement.domain.dtos.inventory.StockVerifyResponse;
import com.siddhesh.inventoryManagement.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/{id}/stock-verify")
    public ResponseEntity<StockVerifyResponse> verifyStock(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.verifyStock(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/purchase")
    public ResponseEntity<StockVerifyResponse> purchase(
            @RequestBody PurchaseRequest payload,
            @RequestParam UUID adminUserId) {
        StockVerifyResponse result = inventoryService.purchase(payload, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/{id}/stock-reconcile")
    public ResponseEntity<StockVerifyResponse> reconcile(
            @PathVariable UUID id,
            @RequestParam UUID adminUserId) {
        return ResponseEntity.ok(inventoryService.reconcile(id, adminUserId));
    }
}
