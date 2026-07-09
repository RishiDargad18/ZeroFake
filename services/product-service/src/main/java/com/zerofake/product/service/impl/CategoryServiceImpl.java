package com.zerofake.product.service.impl;

import com.zerofake.product.dto.common.ApiResponse;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (productCategoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Category with name '" + request.getName() + "' already exists.");
        }

        ProductCategory category = productCategoryMapper.toEntity(request);

        //category.setActive(true);

        ProductCategory savedCategory = productCategoryRepository.save(category);

        return productCategoryMapper.toResponse(savedCategory);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return productCategoryRepository.findByActiveTrue()
                .stream()
                .map(productCategoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {

        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id));

        return productCategoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {

        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(request.getName())
                && productCategoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Category with name '" + request.getName() + "' already exists.");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        ProductCategory updatedCategory = productCategoryRepository.save(category);

        return productCategoryMapper.toResponse(updatedCategory);
    }

    @Override
    public ApiResponse<Void> deleteCategory(UUID id) {

        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(false);

        productCategoryRepository.save(category);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Category deleted successfully.")
                .build();
    }
}