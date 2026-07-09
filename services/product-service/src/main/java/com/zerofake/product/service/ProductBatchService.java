package com.zerofake.product.service;

import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.response.BatchResponse;

import java.util.List;
import java.util.UUID;

public interface ProductBatchService {

    BatchResponse createBatch(CreateBatchRequest request);

    List<BatchResponse> getAllBatches();

    BatchResponse getBatchById(UUID id);

    BatchResponse updateBatch(UUID id, UpdateBatchRequest request);

    ApiResponse<Void> deleteBatch(UUID id);

    List<BatchResponse> getBatchesByProduct(UUID productId);

}