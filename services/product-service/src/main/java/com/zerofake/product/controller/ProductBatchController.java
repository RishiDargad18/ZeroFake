package com.zerofake.product.controller;

import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.response.BatchResponse;
import com.zerofake.product.service.ProductBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@Tag(
        name = "Batch Management",
        description = "APIs for managing product batches"
)
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @Operation(summary = "Create a new product batch")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Batch created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Batch already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @Valid @RequestBody CreateBatchRequest request) {

        BatchResponse response = productBatchService.createBatch(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BatchResponse>builder()
                        .success(true)
                        .message("Batch created successfully.")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Retrieve all product batches")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batches retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getAllBatches() {

        List<BatchResponse> response = productBatchService.getAllBatches();

        return ResponseEntity.ok(
                ApiResponse.<List<BatchResponse>>builder()
                        .success(true)
                        .message("Batches retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Retrieve a product batch by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Batch not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatchById(
            @PathVariable UUID id) {

        BatchResponse response = productBatchService.getBatchById(id);

        return ResponseEntity.ok(
                ApiResponse.<BatchResponse>builder()
                        .success(true)
                        .message("Batch retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Update a product batch")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Batch or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Batch already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBatchRequest request) {

        BatchResponse response = productBatchService.updateBatch(id, request);

        return ResponseEntity.ok(
                ApiResponse.<BatchResponse>builder()
                        .success(true)
                        .message("Batch updated successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Delete a product batch")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Batch not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(
            @PathVariable UUID id) {

        return ResponseEntity.ok(productBatchService.deleteBatch(id));
    }

    @Operation(summary = "Retrieve batches by product")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batches retrieved successfully")
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getBatchesByProduct(
            @PathVariable UUID productId) {

        List<BatchResponse> response = productBatchService.getBatchesByProduct(productId);

        return ResponseEntity.ok(
                ApiResponse.<List<BatchResponse>>builder()
                        .success(true)
                        .message("Batches retrieved successfully.")
                        .data(response)
                        .build()
        );
    }
}