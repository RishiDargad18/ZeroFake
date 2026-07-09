package com.zerofake.product.repository;

import com.zerofake.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    Optional<ProductCategory> findByName(String name);

    boolean existsByName(String name);
    List<ProductCategory> findByActiveTrue();
}