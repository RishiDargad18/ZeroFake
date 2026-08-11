package com.zerofake.product.controller;

import com.zerofake.product.dto.common.ApiResponse;
import com.zerofake.product.dto.request.CreateProductRequest;
import com.zerofake.product.dto.request.UpdateProductRequest;
import com.zerofake.product.dto.response.ProductResponse;
import com.zerofake.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Product Management",
        description = "APIs for managing products"
)
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product already exists")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Product created successfully.",
                        response
                ));
    }

    @Operation(summary = "Retrieve all products")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Products retrieved successfully.",
                productService.getAllProducts()
        ));
    }

    @Operation(summary = "Retrieve a product by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product retrieved successfully.",
                productService.getProductById(id)
        ));
    }

    @Operation(
            summary = "Download the product QR code",
            description = "Returns the PNG image of the QR code attached to the product. "
                    + "The QR code encodes the product identifier only; scanning it is not "
                    + "proof of authenticity and must always be followed by verification."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "QR code image returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or QR code not found")
    })
    @GetMapping(value = "/{id}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getProductQrCode(@PathVariable UUID id) {

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .body(productService.getQrCodeImage(id));
    }

    @Operation(summary = "Update a product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product or category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product code already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product updated successfully.",
                productService.updateProduct(id, request)
        ));
    }

    @Operation(
            summary = "Delete a product",
            description = "Soft deletes the product. Its blockchain history is immutable and is retained."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable UUID id) {

        productService.deleteProduct(id);

        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully.", null));
    }

    @Operation(summary = "Retrieve products by category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable UUID categoryId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Products retrieved successfully.",
                productService.getProductsByCategory(categoryId)
        ));
    }

    @Operation(summary = "Retrieve products by manufacturer")
    @GetMapping("/manufacturer/{manufacturerId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByManufacturer(
            @PathVariable UUID manufacturerId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Products retrieved successfully.",
                productService.getProductsByManufacturer(manufacturerId)
        ));
    }

    @Operation(
            summary = "Update product blockchain status",
            description = "Called by the blockchain service once an on-chain registration "
                    + "transaction has been committed."
    )
    @PatchMapping("/{id}/blockchain-status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateBlockchainStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product blockchain status updated successfully.",
                productService.updateBlockchainStatus(id, status)
        ));
    }
}
