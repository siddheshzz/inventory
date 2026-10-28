package com.siddhesh.inventoryManagement.controller;

import com.siddhesh.inventoryManagement.domain.dtos.AddressRequest;
import com.siddhesh.inventoryManagement.domain.dtos.AddressResponse;
import com.siddhesh.inventoryManagement.domain.entities.Address;
import com.siddhesh.inventoryManagement.repositories.AddressRepository;
import com.siddhesh.inventoryManagement.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private final com.siddhesh.inventoryManagement.services.AddressService addresses;
    private final CurrentUser currentUser;
    @GetMapping
    public List<AddressResponse> list() { return addresses.list(currentUser.requireUser().getId()); }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@jakarta.validation.Valid @RequestBody AddressRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addresses.save(currentUser.requireUser().getId(), null, r));
    }

    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable UUID id, @jakarta.validation.Valid @RequestBody AddressRequest r) {
        return addresses.save(currentUser.requireUser().getId(), id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        addresses.delete(currentUser.requireUser().getId(), id);
        return ResponseEntity.noContent().build();
    }
}
