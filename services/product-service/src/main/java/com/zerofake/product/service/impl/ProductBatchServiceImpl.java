package com.zerofake.product.service.impl;

import com.zerofake.product.constant.BatchStatus;
import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.response.BatchResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
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

        Product product = findProduct(request.getProductId());

        validateQuantities(request.getAvailableQuantity(), request.getQuantityProduced());
        validateDates(request.getManufactureDate(), request.getExpiryDate());

        ProductBatch productBatch = productBatchMapper.toEntity(request);
        productBatch.setProduct(product);

        return productBatchMapper.toResponse(productBatchRepository.save(productBatch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {

        return productBatchRepository.findAll()
                .stream()
                .map(productBatchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(UUID id) {
        return productBatchMapper.toResponse(findBatch(id));
    }

    @Override
    public BatchResponse updateBatch(UUID id, UpdateBatchRequest request) {

        ProductBatch productBatch = findBatch(id);

        if (!productBatch.getBatchNumber().equals(request.getBatchNumber())
                && productBatchRepository.existsByBatchNumber(request.getBatchNumber())) {
            throw new ConflictException(
                    "Batch with number '" + request.getBatchNumber() + "' already exists."
            );
        }

        Product product = findProduct(request.getProductId());

        validateQuantities(request.getAvailableQuantity(), request.getQuantityProduced());
        validateDates(request.getManufactureDate(), request.getExpiryDate());

        productBatchMapper.updateEntity(request, productBatch);
        productBatch.setProduct(product);

        return productBatchMapper.toResponse(productBatchRepository.save(productBatch));
    }

    @Override
    public void deleteBatch(UUID id) {

        ProductBatch productBatch = findBatch(id);

        // A batch is never removed: recalling it preserves the manufacturing
        // record that the blockchain history refers to.
        productBatch.setStatus(BatchStatus.RECALLED);

        productBatchRepository.save(productBatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByProduct(UUID productId) {

        return productBatchRepository.findByProductId(productId)
                .stream()
                .map(productBatchMapper::toResponse)
                .toList();
    }

    private void validateQuantities(Integer availableQuantity, Integer quantityProduced) {

        if (availableQuantity > quantityProduced) {
            throw new BadRequestException("Available quantity cannot exceed quantity produced.");
        }
    }

    private void validateDates(LocalDate manufactureDate, LocalDate expiryDate) {

        if (expiryDate != null && !expiryDate.isAfter(manufactureDate)) {
            throw new BadRequestException("Expiry date must be after the manufacture date.");
        }
    }

    private ProductBatch findBatch(UUID id) {

        return productBatchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Batch not found with id: " + id));
    }

    private Product findProduct(UUID productId) {

        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId));
    }
}
