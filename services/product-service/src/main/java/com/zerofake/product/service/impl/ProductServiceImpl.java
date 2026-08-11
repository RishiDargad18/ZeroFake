package com.zerofake.product.service.impl;

import com.zerofake.product.constant.BlockchainStatus;
import com.zerofake.product.dto.request.CreateProductRequest;
import com.zerofake.product.dto.request.UpdateProductRequest;
import com.zerofake.product.dto.response.ProductResponse;
import com.zerofake.product.entity.Product;
import com.zerofake.product.entity.ProductCategory;
import com.zerofake.product.exception.BadRequestException;
import com.zerofake.product.exception.ConflictException;
import com.zerofake.product.exception.ResourceNotFoundException;
import com.zerofake.product.mapper.ProductMapper;
import com.zerofake.product.repository.ProductCategoryRepository;
import com.zerofake.product.repository.ProductRepository;
import com.zerofake.product.service.ProductService;
import com.zerofake.product.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;
    private final QrCodeService qrCodeService;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new ConflictException(
                    "Product with code '" + request.getProductCode() + "' already exists."
            );
        }

        ProductCategory category = findCategory(request.getCategoryId());

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        // The QR code encodes the generated identifier, so it can only be
        // produced once the product has been persisted.
        savedProduct.setQrCodePath(qrCodeService.generateForProduct(savedProduct.getId()));

        return productMapper.toResponse(productRepository.save(savedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return productMapper.toResponse(findActiveProduct(id));
    }

    @Override
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {

        Product product = findActiveProduct(id);

        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new ConflictException(
                    "Product with code '" + request.getProductCode() + "' already exists."
            );
        }

        ProductCategory category = findCategory(request.getCategoryId());

        productMapper.updateEntity(request, product);
        product.setCategory(category);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {

        Product product = findActiveProduct(id);

        product.setActive(false);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {

        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByManufacturer(UUID manufacturerId) {

        return productRepository.findByManufacturerIdAndActiveTrue(manufacturerId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse updateBlockchainStatus(UUID id, String status) {

        Product product = findActiveProduct(id);

        product.setBlockchainStatus(parseBlockchainStatus(status));

        Product savedProduct = productRepository.save(product);

        log.info("Product {} blockchain status updated to {}", id, savedProduct.getBlockchainStatus());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getQrCodeImage(UUID id) {
        return qrCodeService.readQrCode(findActiveProduct(id).getQrCodePath());
    }

    private BlockchainStatus parseBlockchainStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new BadRequestException("Blockchain status is required.");
        }

        try {
            return BlockchainStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid blockchain status '" + status + "'. Expected one of: "
                            + Arrays.stream(BlockchainStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", "))
            );
        }
    }

    private Product findActiveProduct(UUID id) {

        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductCategory findCategory(UUID categoryId) {

        return productCategoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found with id: " + categoryId));
    }
}
