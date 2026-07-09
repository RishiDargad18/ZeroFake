package com.zerofake.product.service;

import com.zerofake.product.dto.request.CreateProductRequest;
import com.zerofake.product.dto.request.UpdateProductRequest;
import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.response.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(UUID id);

    ProductResponse updateProduct(UUID id, UpdateProductRequest request);

    ApiResponse<Void> deleteProduct(UUID id);

    List<ProductResponse> getProductsByCategory(UUID categoryId);

    List<ProductResponse> getProductsByManufacturer(UUID manufacturerId);

}