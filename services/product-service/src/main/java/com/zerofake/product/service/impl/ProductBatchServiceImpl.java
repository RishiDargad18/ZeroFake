package com.zerofake.product.service.impl;

import com.zerofake.product.constant.BatchStatus;
import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.response.BatchResponse;
import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.entity.Product;
import com.zerofake.product.entity.ProductBatch;
import com.zerofake.product.exception.BadRequestException;
import com.zerofake.product.exception.ConflictException;
import com.zerofake.product.exception.ResourceNotFoundException;
import com.zerofake.product.mapper.ProductBatchMapper;
import com.zerofake.product.repository.ProductBatchRepository;
import com.zerofake.product.repository.ProductRepository;
import com.zerofake.product.service.ProductBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository productBatchRepository;
    private final ProductRepository productRepository;
    private final ProductBatchMapper productBatchMapper;

    @Override
    public BatchResponse createBatch(CreateBatchRequest request) {

        if (productBatchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new ConflictException(
                    "Batch with number '" + request.getBatchNumber() + "' already exists."
            );
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + request.getProductId()
                        ));

        if (request.getExpiryDate() != null
                && !request.getExpiryDate().isAfter(request.getManufactureDate())) {
            throw new BadRequestException(
                    "Expiry date must be after manufacture date."
            );
        }

        if (request.getAvailableQuantity() > request.getQuantityProduced()) {
            throw new BadRequestException(
                    "Available quantity cannot exceed quantity produced."
            );
        }

        ProductBatch productBatch = productBatchMapper.toEntity(request);

        productBatch.setProduct(product);

        ProductBatch savedBatch = productBatchRepository.save(productBatch);

        return productBatchMapper.toResponse(savedBatch);
    }
    @Override
    public List<BatchResponse> getAllBatches() {
        return productBatchRepository.findAll()
                .stream()
                .map(productBatchMapper::toResponse)
                .toList();
    }

    @Override
    public BatchResponse getBatchById(UUID id) {

        ProductBatch productBatch = productBatchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Batch not found with id: " + id));

        return productBatchMapper.toResponse(productBatch);
    }

    @Override
    public BatchResponse updateBatch(UUID id, UpdateBatchRequest request) {

        ProductBatch productBatch = productBatchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Batch not found with id: " + id));

        if (!productBatch.getBatchNumber().equals(request.getBatchNumber())
                && productBatchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new ConflictException(
                    "Batch with number '" + request.getBatchNumber() + "' already exists."
            );
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + request.getProductId()
                        ));

        if (request.getExpiryDate() != null
                && !request.getExpiryDate().isAfter(request.getManufactureDate())) {
            throw new BadRequestException(
                    "Expiry date must be after manufacture date."
            );
        }

        if (request.getAvailableQuantity() > request.getQuantityProduced()) {
            throw new BadRequestException(
                    "Available quantity cannot exceed quantity produced."
            );
        }

        productBatch.setBatchNumber(request.getBatchNumber());
        productBatch.setManufactureDate(request.getManufactureDate());
        productBatch.setExpiryDate(request.getExpiryDate());
        productBatch.setQuantityProduced(request.getQuantityProduced());
        productBatch.setAvailableQuantity(request.getAvailableQuantity());
        productBatch.setManufacturingLocation(request.getManufacturingLocation());
        productBatch.setRemarks(request.getRemarks());
        productBatch.setProduct(product);

        ProductBatch updatedBatch = productBatchRepository.save(productBatch);

        return productBatchMapper.toResponse(updatedBatch);
    }
    @Override
    public ApiResponse<Void> deleteBatch(UUID id) {

        ProductBatch productBatch = productBatchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Batch not found with id: " + id));

        productBatch.setStatus(BatchStatus.RECALLED);

        productBatchRepository.save(productBatch);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Batch deleted successfully.")
                .build();
    }

    @Override
    public List<BatchResponse> getBatchesByProduct(UUID productId) {
        return productBatchRepository.findByProductId(productId)
                .stream()
                .map(productBatchMapper::toResponse)
                .toList();
    }
}