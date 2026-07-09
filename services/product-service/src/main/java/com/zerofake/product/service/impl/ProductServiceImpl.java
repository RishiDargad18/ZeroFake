package com.zerofake.product.service.impl;

import com.zerofake.product.dto.request.CreateProductRequest;
import com.zerofake.product.dto.request.UpdateProductRequest;
import com.zerofake.product.dto.response.ProductResponse;
import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.entity.Product;
import com.zerofake.product.entity.ProductCategory;
import com.zerofake.product.exception.ConflictException;
import com.zerofake.product.exception.ResourceNotFoundException;
import com.zerofake.product.mapper.ProductMapper;
import com.zerofake.product.repository.ProductCategoryRepository;
import com.zerofake.product.repository.ProductRepository;
import com.zerofake.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new ConflictException(
                    "Product with code '" + request.getProductCode() + "' already exists."
            );
        }

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()
                ));

        Product product = productMapper.toEntity(request);

        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
    @Override
    public List<ProductResponse> getAllProducts() {

        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
    @Override
    public ProductResponse getProductById(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        return productMapper.toResponse(product);
    }
    @Override
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new ConflictException(
                    "Product with code '" + request.getProductCode() + "' already exists."
            );
        }

        ProductCategory category = productCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + request.getCategoryId()
                        ));

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setManufacturerId(request.getManufacturerId());
        product.setImageUrl(request.getImageUrl());
        product.setImageFileName(request.getImageFileName());
        product.setImageContentType(request.getImageContentType());
        product.setImageSize(request.getImageSize());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }
    @Override
    public ApiResponse<Void> deleteProduct(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));

        product.setActive(false);

        productRepository.save(product);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Product deleted successfully.")
                .build();
    }
    @Override
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
    @Override
    public List<ProductResponse> getProductsByManufacturer(UUID manufacturerId) {
        return productRepository.findByManufacturerId(manufacturerId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
}