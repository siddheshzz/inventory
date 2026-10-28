package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.Address;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUser_IdOrderByIsDefaultDescCreatedAtDesc(UUID userId);
}
