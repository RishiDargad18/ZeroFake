package com.zerofake.product.service.impl;

import com.zerofake.product.dto.request.CreateCategoryRequest;
import com.zerofake.product.dto.request.UpdateCategoryRequest;
import com.zerofake.product.dto.response.CategoryResponse;
import com.zerofake.product.entity.ProductCategory;
import com.zerofake.product.exception.ConflictException;
import com.zerofake.product.exception.ResourceNotFoundException;
import com.zerofake.product.mapper.ProductCategoryMapper;
import com.zerofake.product.repository.ProductCategoryRepository;
import com.zerofake.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (productCategoryRepository.existsByName(request.getName())) {
            throw new ConflictException(
                    "Category with name '" + request.getName() + "' already exists."
            );
        }

        ProductCategory category = productCategoryMapper.toEntity(request);

        return productCategoryMapper.toResponse(productCategoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return productCategoryRepository.findByActiveTrue()
                .stream()
                .map(productCategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        return productCategoryMapper.toResponse(findActiveCategory(id));
    }

    @Override
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {

        ProductCategory category = findActiveCategory(id);

        if (!category.getName().equals(request.getName())
                && productCategoryRepository.existsByName(request.getName())) {
            throw new ConflictException(
                    "Category with name '" + request.getName() + "' already exists."
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return productCategoryMapper.toResponse(productCategoryRepository.save(category));
    }

    @Override
    public void deleteCategory(UUID id) {

        ProductCategory category = findActiveCategory(id);

        category.setActive(false);

        productCategoryRepository.save(category);
    }

    private ProductCategory findActiveCategory(UUID id) {

        return productCategoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id));
    }
}
