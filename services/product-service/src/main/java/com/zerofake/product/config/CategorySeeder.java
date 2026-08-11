package com.zerofake.product.config;

import com.zerofake.product.entity.ProductCategory;
import com.zerofake.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seeds the product categories that the platform demonstrates counterfeiting
 * scenarios against.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "zerofake.seed",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CategorySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategorySeeder.class);

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public void run(String... args) {
        seedCategory("Electronics", "Electronic goods and consumer devices");
        seedCategory("Luxury Goods", "High-end designer products and accessories");
        seedCategory("Pharmaceuticals", "Medicines, vaccines and clinical products");
        seedCategory("Cosmetics", "Skincare, makeup and beauty products");
    }

    private void seedCategory(String name, String description) {

        if (productCategoryRepository.existsByName(name)) {
            return;
        }

        productCategoryRepository.save(
                ProductCategory.builder()
                        .name(name)
                        .description(description)
                        .active(true)
                        .build()
        );

        log.info("Seeded product category: {}", name);
    }
}
