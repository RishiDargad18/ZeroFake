package com.zerofake.product.controller;

import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.request.CreateBatchRequest;
import com.zerofake.product.dto.request.UpdateBatchRequest;
import com.zerofake.product.dto.response.BatchResponse;
import com.zerofake.product.service.ProductBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Batch Management",
        description = "APIs for managing product batches"
)
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @Operation(summary = "Create a new product batch")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Batch created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Batch already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BatchResponse>> createBatch(
            @Valid @RequestBody CreateBatchRequest request) {

        BatchResponse response = productBatchService.createBatch(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Batch created successfully.",
                        response
                ));
    }

    @Operation(summary = "Retrieve all product batches")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getAllBatches() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Batches retrieved successfully.",
                productBatchService.getAllBatches()
        ));
    }

    @Operation(summary = "Retrieve a product batch by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Batch not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> getBatchById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Batch retrieved successfully.",
                productBatchService.getBatchById(id)
        ));
    }

    @Operation(summary = "Update a product batch")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Batch or product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Batch number already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BatchResponse>> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBatchRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Batch updated successfully.",
                productBatchService.updateBatch(id, request)
        ));
    }

    @Operation(
            summary = "Recall a product batch",
            description = "Marks the batch as RECALLED. Manufacturing records are retained."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBatch(
            @PathVariable UUID id) {

        productBatchService.deleteBatch(id);

        return ResponseEntity.ok(ApiResponse.ok("Batch deleted successfully.", null));
    }

    @Operation(summary = "Retrieve batches by product")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<BatchResponse>>> getBatchesByProduct(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Batches retrieved successfully.",
                productBatchService.getBatchesByProduct(productId)
        ));
    }
}
