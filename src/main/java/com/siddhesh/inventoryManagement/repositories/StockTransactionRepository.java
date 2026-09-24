package com.siddhesh.inventoryManagement.repositories;

import com.siddhesh.inventoryManagement.domain.entities.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {

    @Query("SELECT COALESCE(SUM(t.quantityChange), 0) FROM StockTransaction t WHERE t.product.id = :productId")
    long sumQuantityChangeByProductId(@Param("productId") UUID productId);

    List<StockTransaction> findByOrderItem_Id(UUID orderItemId);
}
