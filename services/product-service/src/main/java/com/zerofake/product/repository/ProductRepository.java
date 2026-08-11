package com.zerofake.product.repository;

import com.zerofake.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByProductCode(String productCode);

    Optional<Product> findByIdAndActiveTrue(UUID id);

    boolean existsByProductCode(String productCode);

    List<Product> findByCategoryIdAndActiveTrue(UUID categoryId);

    List<Product> findByManufacturerIdAndActiveTrue(UUID manufacturerId);

    List<Product> findByActiveTrue();
}
