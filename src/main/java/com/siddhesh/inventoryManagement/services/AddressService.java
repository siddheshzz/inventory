package com.siddhesh.inventoryManagement.services;

import com.siddhesh.inventoryManagement.domain.dtos.*;
import com.siddhesh.inventoryManagement.domain.entities.Address;
import com.siddhesh.inventoryManagement.repositories.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addresses.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(userId).stream().map(this::response).toList();
    }

    @Transactional
    public AddressResponse save(UUID userId, UUID id, AddressRequest r) {
        var user = users.findByIdForUpdate(userId).orElseThrow();
        Address a = id == null ? Address.builder().user(user).build() : owned(userId, id);
        var existing = addresses.findByUser_IdOrderByIsDefaultDescCreatedAtDesc(userId);
        boolean primary = Boolean.TRUE.equals(r.isDefault()) || existing.isEmpty();
        if (primary) existing.forEach(other -> other.setIsDefault(false));
        a.setAddressLine1(r.addressLine1().trim()); a.setAddressLine2(r.addressLine2());
        a.setCity(r.city().trim()); a.setState(r.state().trim());
        a.setCountry(r.country().trim()); a.setPostalCode(r.postalCode().trim()); a.setIsDefault(primary);
        return response(addresses.save(a));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        users.findByIdForUpdate(userId).orElseThrow();
        addresses.delete(owned(userId, id));
    }

    private Address owned(UUID userId, UUID id) {
        return addresses.findById(id).filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }
    private AddressResponse response(Address a) {
        return new AddressResponse(a.getId(), a.getAddressLine1(), a.getAddressLine2(), a.getCity(), a.getState(), a.getPostalCode(), a.getCountry(), a.getIsDefault(), a.getCreatedAt());
    }
}
