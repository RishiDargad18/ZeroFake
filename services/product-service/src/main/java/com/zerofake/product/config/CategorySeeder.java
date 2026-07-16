package com.zerofake.product.config;

import com.zerofake.product.entity.ProductCategory;
import com.zerofake.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        seedCategory("Electronics", "Electronic goods and consumer devices");
        seedCategory("Luxury Goods", "High-end designer products and accessories");
        seedCategory("Pharmaceuticals", "Medicines, vaccines and clinical products");
        seedCategory("Cosmetics", "Skincare, makeup and beauty products");
    }

    private void seedCategory(String name, String description) {
        if (!productCategoryRepository.existsByName(name)) {
            ProductCategory category = ProductCategory.builder()
                    .name(name)
                    .description(description)
                    .active(true)
                    .build();
            productCategoryRepository.save(category);
            System.out.println("Seeded category: " + name);
        }
    }
}
