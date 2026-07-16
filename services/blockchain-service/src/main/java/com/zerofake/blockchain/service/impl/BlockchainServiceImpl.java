package com.zerofake.blockchain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;
import com.zerofake.blockchain.entity.BlockchainTransaction;
import com.zerofake.blockchain.exception.ResourceNotFoundException;
import com.zerofake.blockchain.fabric.FabricContractService;
import com.zerofake.blockchain.mapper.BlockchainTransactionMapper;
import com.zerofake.blockchain.repository.BlockchainTransactionRepository;
import com.zerofake.blockchain.service.BlockchainService;
import com.zerofake.blockchain.dto.response.ProductHistoryItemResponse;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Proposal;
import org.hyperledger.fabric.client.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {


    private final BlockchainTransactionRepository blockchainTransactionRepository;

    private final BlockchainTransactionMapper blockchainTransactionMapper;

    private final FabricContractService fabricContractService;

    private final ObjectMapper objectMapper;

    @Override
    public BlockchainTransactionResponse getTransactionByTransactionId(String transactionId) {

        return blockchainTransactionMapper.toResponse(
                blockchainTransactionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Blockchain transaction not found with transaction ID: " + transactionId))
        );
    }

    @Override
    public List<BlockchainTransactionResponse> getTransactionsByProductId(UUID productId) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByProductId(productId)
        );
    }

    @Override
    public List<BlockchainTransactionResponse> getTransactionsByStatus(BlockchainStatus status) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByStatus(status)
        );
    }

    @Override
    public List<BlockchainTransactionResponse> getTransactionsByTransactionType(TransactionType transactionType) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByTransactionType(transactionType)
        );
    }
    @Override
    public List<BlockchainTransactionResponse> getAllTransactions() {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findAll()
        );
    }
    @Override
    public BlockchainTransactionResponse registerProduct(RegisterProductRequest request) {

        try {
            Proposal proposal = fabricContractService
                    .newProposal("RegisterProduct")
                    .addArguments(
                            request.getProductId().toString(),
                            request.getManufacturerId().toString()
                    )
                    .build();

            Transaction transaction = proposal.endorse();

            byte[] result = transaction.submit();

            String transactionId = transaction.getTransactionId();

            objectMapper.readTree(result);

            BlockchainTransaction blockchainTransaction = BlockchainTransaction.builder()
                    .productId(request.getProductId())
                    .transactionId(transactionId)
                    .transactionType(TransactionType.PRODUCT_REGISTERED)
                    .performedBy(request.getManufacturerId())
                    .status(BlockchainStatus.SUCCESS)
                    .message("Product registered successfully on blockchain.")
                    .blockNumber(null)
                    .blockHash(null)
                    .build();

            BlockchainTransaction savedTransaction =
                    blockchainTransactionRepository.save(blockchainTransaction);

            return blockchainTransactionMapper.toResponse(savedTransaction);

        } catch (Exception exception) {
            // Walk the entire exception chain to find "already exists"
            boolean alreadyExists = false;
            Throwable t = exception;
            while (t != null) {
                String msg = t.getMessage();
                if (msg != null && msg.toLowerCase().contains("already exists")) {
                    alreadyExists = true;
                    break;
                }
                // Also check suppressed exceptions
                for (Throwable suppressed : t.getSuppressed()) {
                    String sMsg = suppressed.getMessage();
                    if (sMsg != null && sMsg.toLowerCase().contains("already exists")) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (alreadyExists) break;
                t = t.getCause();
            }

            // For EndorseException, also check getDetails() if available
            if (!alreadyExists && exception instanceof org.hyperledger.fabric.client.EndorseException endorseEx) {
                for (var detail : endorseEx.getDetails()) {
                    if (detail.toString().toLowerCase().contains("already exists")) {
                        alreadyExists = true;
                        break;
                    }
                }
            }
            // Check cause for EndorseException too
            if (!alreadyExists) {
                Throwable c = exception.getCause();
                while (c != null) {
                    if (c instanceof org.hyperledger.fabric.client.EndorseException endorseEx2) {
                        for (var detail : endorseEx2.getDetails()) {
                            if (detail.toString().toLowerCase().contains("already exists")) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }
                    if (alreadyExists) break;
                    c = c.getCause();
                }
            }

            // Last resort: check full stack trace string
            if (!alreadyExists) {
                java.io.StringWriter sw = new java.io.StringWriter();
                exception.printStackTrace(new java.io.PrintWriter(sw));
                if (sw.toString().toLowerCase().contains("already exists")) {
                    alreadyExists = true;
                }
            }

            if (alreadyExists) {
                // Product is already on-chain — treat as idempotent success
                BlockchainTransaction blockchainTransaction = BlockchainTransaction.builder()
                        .productId(request.getProductId())
                        .transactionId("ALREADY_REGISTERED_" + java.util.UUID.randomUUID())
                        .transactionType(TransactionType.PRODUCT_REGISTERED)
                        .performedBy(request.getManufacturerId())
                        .status(BlockchainStatus.SUCCESS)
                        .message("Product was already registered on blockchain.")
                        .blockNumber(null)
                        .blockHash(null)
                        .build();

                BlockchainTransaction savedTransaction =
                        blockchainTransactionRepository.save(blockchainTransaction);

                return blockchainTransactionMapper.toResponse(savedTransaction);
            }

            throw new RuntimeException(
                    "Failed to register product on Hyperledger Fabric.",
                    exception
            );
        }
    }
    @Override
    public BlockchainTransactionResponse transferOwnership(TransferOwnershipRequest request) {

        try {
            Proposal proposal = fabricContractService
                    .newProposal("TransferOwnership")
                    .addArguments(
                            request.getProductId().toString(),
                            request.getFromOwnerId().toString(),
                            request.getToOwnerId().toString(),
                            request.getToOwnerRole().name()
                    )
                    .build();

            Transaction transaction = proposal.endorse();

            byte[] result = transaction.submit();

            String transactionId = transaction.getTransactionId();

            objectMapper.readTree(result);

            BlockchainTransaction blockchainTransaction = BlockchainTransaction.builder()
                    .productId(request.getProductId())
                    .transactionId(transactionId)
                    .transactionType(TransactionType.OWNERSHIP_TRANSFERRED)
                    .performedBy(request.getFromOwnerId())
                    .status(BlockchainStatus.SUCCESS)
                    .message("Ownership transferred successfully on blockchain.")
                    .blockNumber(null)
                    .blockHash(null)
                    .build();

            BlockchainTransaction savedTransaction =
                    blockchainTransactionRepository.save(blockchainTransaction);

            return blockchainTransactionMapper.toResponse(savedTransaction);

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to transfer ownership on Hyperledger Fabric.",
                    exception
            );
        }
    }
    @Override
    public VerificationResponse verifyProduct(VerifyProductRequest request) {

        try {
            // VerifyProduct is read-only — use evaluateTransaction (no ledger write).
            Contract contract = fabricContractService.getContract();

            byte[] result = contract.evaluateTransaction(
                    "VerifyProduct",
                    request.getProductId().toString()
            );

            JsonNode productAsset = objectMapper.readTree(result);

            return VerificationResponse.builder()
                    .productId(request.getProductId())
                    .authentic(productAsset.path("isVerified").asBoolean())
                    .message("Product verification completed successfully.")
                    .transactionId("query-" + UUID.randomUUID())
                    .build();

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to verify product on Hyperledger Fabric.",
                    exception
            );
        }
    }
    @Override
    public ProductHistoryResponse getProductHistory(UUID productId) {

        try {
            // GetProductHistory uses GetHistoryForKey which is read-only — use evaluateTransaction.
            Contract contract = fabricContractService.getContract();

            byte[] result = contract.evaluateTransaction(
                    "GetProductHistory",
                    productId.toString()
            );

            JsonNode historyArray = objectMapper.readTree(result);

            List<ProductHistoryItemResponse> historyItems = new ArrayList<>();

            if (historyArray != null && historyArray.isArray()) {
                for (JsonNode product : historyArray) {

                    ProductHistoryItemResponse historyItem = ProductHistoryItemResponse.builder()
                            .manufacturerId(UUID.fromString(product.path("manufacturerId").asText()))
                            .currentOwnerId(UUID.fromString(product.path("currentOwnerId").asText()))
                            .currentOwnerRole(product.path("currentOwnerRole").asText())
                            .productStatus(product.path("productStatus").asText())
                            .verified(product.path("isVerified").asBoolean())
                            .createdAt(product.path("createdAt").asText())
                            .updatedAt(product.path("updatedAt").asText())
                            .build();

                    historyItems.add(historyItem);
                }
            }

            return ProductHistoryResponse.builder()
                    .productId(productId)
                    .history(historyItems)
                    .build();

        } catch (Exception exception) {
            throw new RuntimeException(
                    "Failed to retrieve product history from Hyperledger Fabric.",
                    exception
            );
        }
    }
}