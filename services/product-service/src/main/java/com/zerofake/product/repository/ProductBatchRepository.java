package com.zerofake.product.repository;

import com.zerofake.product.entity.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, UUID> {

    Optional<ProductBatch> findByBatchNumber(String batchNumber);

    boolean existsByBatchNumber(String batchNumber);

    List<ProductBatch> findByProductId(UUID productId);

}