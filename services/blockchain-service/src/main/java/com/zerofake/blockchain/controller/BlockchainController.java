package com.zerofake.blockchain.controller;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.common.ApiResponse;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;
import com.zerofake.blockchain.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blockchain")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Blockchain API",
        description = "Ledger operations backed by Hyperledger Fabric."
)
public class BlockchainController {

    private final BlockchainService blockchainService;

    @Operation(
            summary = "Register a product on the blockchain",
            description = "Anchors the product's identity on the ledger and marks it "
                    + "REGISTERED in the product service."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product registered on the ledger"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product is already registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "The blockchain network could not service the request")
    })
    @PostMapping("/register-product")
    public ResponseEntity<ApiResponse<BlockchainTransactionResponse>> registerProduct(
            @Valid @RequestBody RegisterProductRequest request) {

        BlockchainTransactionResponse response = blockchainService.registerProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Product registered on the blockchain.",
                        response
                ));
    }

    @Operation(summary = "Transfer product ownership on the blockchain")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ownership transferred"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product is not registered on the ledger"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "The blockchain network could not service the request")
    })
    @PostMapping("/transfer-ownership")
    public ResponseEntity<ApiResponse<BlockchainTransactionResponse>> transferOwnership(
            @Valid @RequestBody TransferOwnershipRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Ownership transferred successfully.",
                blockchainService.transferOwnership(request)
        ));
    }

    @Operation(
            summary = "Verify a product against the ledger",
            description = "Reads the product's current on-chain state. A 404 means the "
                    + "product has no blockchain identity, which the fraud detection "
                    + "service treats as a counterfeit signal."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product state returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product is not registered on the ledger"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "The blockchain network could not service the request")
    })
    @PostMapping("/verify-product")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyProduct(
            @Valid @RequestBody VerifyProductRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product verification completed.",
                blockchainService.verifyProduct(request)
        ));
    }

    @Operation(summary = "Retrieve the immutable ledger history of a product")
    @GetMapping("/products/{productId}/history")
    public ResponseEntity<ApiResponse<ProductHistoryResponse>> getProductHistory(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product history retrieved successfully.",
                blockchainService.getProductHistory(productId)
        ));
    }

    @Operation(summary = "Retrieve all blockchain transaction records")
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<BlockchainTransactionResponse>>> getAllTransactions() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Transactions retrieved successfully.",
                blockchainService.getAllTransactions()
        ));
    }

    @Operation(summary = "Retrieve a transaction record by Fabric transaction ID")
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<BlockchainTransactionResponse>> getTransactionByTransactionId(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Transaction retrieved successfully.",
                blockchainService.getTransactionByTransactionId(transactionId)
        ));
    }

    @Operation(summary = "Retrieve all transaction records for a product")
    @GetMapping("/transactions/product/{productId}")
    public ResponseEntity<ApiResponse<List<BlockchainTransactionResponse>>> getTransactionsByProductId(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Transactions retrieved successfully.",
                blockchainService.getTransactionsByProductId(productId)
        ));
    }

    @Operation(summary = "Retrieve transaction records by status")
    @GetMapping("/transactions/status/{status}")
    public ResponseEntity<ApiResponse<List<BlockchainTransactionResponse>>> getTransactionsByStatus(
            @PathVariable BlockchainStatus status) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Transactions retrieved successfully.",
                blockchainService.getTransactionsByStatus(status)
        ));
    }

    @Operation(summary = "Retrieve transaction records by type")
    @GetMapping("/transactions/type/{transactionType}")
    public ResponseEntity<ApiResponse<List<BlockchainTransactionResponse>>> getTransactionsByTransactionType(
            @PathVariable TransactionType transactionType) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Transactions retrieved successfully.",
                blockchainService.getTransactionsByTransactionType(transactionType)
        ));
    }
}
