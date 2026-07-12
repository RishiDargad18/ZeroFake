package com.zerofake.blockchain.controller;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;
import com.zerofake.blockchain.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blockchain")
@RequiredArgsConstructor
@Tag(
        name = "Blockchain API",
        description = "APIs for interacting with the Hyperledger Fabric blockchain."
)
public class BlockchainController {

    private final BlockchainService blockchainService;

    @PostMapping("/register-product")
    @Operation(
            summary = "Register Product",
            description = "Registers a product on the Hyperledger Fabric blockchain."
    )
    @ApiResponse(responseCode = "200", description = "Product registered successfully.")
    public ResponseEntity<BlockchainTransactionResponse> registerProduct(
            @Valid @RequestBody RegisterProductRequest request) {

        return ResponseEntity.ok(
                blockchainService.registerProduct(request)
        );
    }

    @PostMapping("/transfer-ownership")
    @Operation(
            summary = "Transfer Ownership",
            description = "Transfers product ownership on the Hyperledger Fabric blockchain."
    )
    @ApiResponse(responseCode = "200", description = "Ownership transferred successfully.")
    public ResponseEntity<BlockchainTransactionResponse> transferOwnership(
            @Valid @RequestBody TransferOwnershipRequest request) {

        return ResponseEntity.ok(
                blockchainService.transferOwnership(request)
        );
    }

    @PostMapping("/verify-product")
    @Operation(
            summary = "Verify Product",
            description = "Verifies product authenticity using the Hyperledger Fabric blockchain."
    )
    @ApiResponse(responseCode = "200", description = "Product verification completed.")
    public ResponseEntity<VerificationResponse> verifyProduct(
            @Valid @RequestBody VerifyProductRequest request) {

        return ResponseEntity.ok(
                blockchainService.verifyProduct(request)
        );
    }

    @GetMapping("/products/{productId}/history")
    @Operation(
            summary = "Get Product History",
            description = "Retrieves the complete blockchain history of a product."
    )
    @ApiResponse(responseCode = "200", description = "Product history retrieved successfully.")
    public ResponseEntity<ProductHistoryResponse> getProductHistory(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(
                blockchainService.getProductHistory(productId)
        );
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(
            summary = "Get Transaction by Transaction ID",
            description = "Retrieves blockchain transaction metadata by Fabric transaction ID."
    )
    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully.")
    public ResponseEntity<BlockchainTransactionResponse> getTransactionByTransactionId(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(
                blockchainService.getTransactionByTransactionId(transactionId)
        );
    }

    @GetMapping("/transactions/product/{productId}")
    @Operation(
            summary = "Get Transactions by Product",
            description = "Retrieves all blockchain transactions associated with a product."
    )
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully.")
    public ResponseEntity<List<BlockchainTransactionResponse>> getTransactionsByProductId(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(
                blockchainService.getTransactionsByProductId(productId)
        );
    }

    @GetMapping("/transactions/status/{status}")
    @Operation(
            summary = "Get Transactions by Status",
            description = "Retrieves blockchain transactions filtered by blockchain status."
    )
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully.")
    public ResponseEntity<List<BlockchainTransactionResponse>> getTransactionsByStatus(
            @PathVariable BlockchainStatus status) {

        return ResponseEntity.ok(
                blockchainService.getTransactionsByStatus(status)
        );
    }

    @GetMapping("/transactions/type/{transactionType}")
    @Operation(
            summary = "Get Transactions by Type",
            description = "Retrieves blockchain transactions filtered by transaction type."
    )
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully.")
    public ResponseEntity<List<BlockchainTransactionResponse>> getTransactionsByTransactionType(
            @PathVariable TransactionType transactionType) {

        return ResponseEntity.ok(
                blockchainService.getTransactionsByTransactionType(transactionType)
        );
    }
}