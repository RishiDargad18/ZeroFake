package com.zerofake.product.service;

import com.zerofake.product.dto.request.CreateCategoryRequest;
import com.zerofake.product.dto.request.UpdateCategoryRequest;
import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);

    ApiResponse<Void> deleteCategory(UUID id);

}