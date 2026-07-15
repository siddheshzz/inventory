package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.redis.OtpCache;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface OtpCacheRepository extends CrudRepository<OtpCache, String> {
    // Spring Data Redis automatically generates key lookups under the hood
}
