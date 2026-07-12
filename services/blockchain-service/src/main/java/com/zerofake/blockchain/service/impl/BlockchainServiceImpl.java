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
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Proposal;
import org.hyperledger.fabric.client.Transaction;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.zerofake.blockchain.dto.response.ProductHistoryItemResponse;

import java.util.ArrayList;
import java.util.List;
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
            Proposal proposal = fabricContractService
                    .newProposal("VerifyProduct")
                    .addArguments(
                            request.getProductId().toString()
                    )
                    .build();

            Transaction transaction = proposal.endorse();

            byte[] result = transaction.submit();

            JsonNode productAsset = objectMapper.readTree(result);

            return VerificationResponse.builder()
                    .productId(request.getProductId())
                    .authentic(productAsset.path("isVerified").asBoolean())
                    .message("Product verification completed successfully.")
                    .transactionId(transaction.getTransactionId())
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
            Proposal proposal = fabricContractService
                    .newProposal("GetProductHistory")
                    .addArguments(productId.toString())
                    .build();

            Transaction transaction = proposal.endorse();

            byte[] result = transaction.submit();

            JsonNode historyArray = objectMapper.readTree(result);

            List<ProductHistoryItemResponse> historyItems = new ArrayList<>();

            for (JsonNode product : historyArray) {

                ProductHistoryItemResponse historyItem = ProductHistoryItemResponse.builder()
                        .manufacturerId(UUID.fromString(product.get("manufacturerId").asText()))
                        .currentOwnerId(UUID.fromString(product.get("currentOwnerId").asText()))
                        .currentOwnerRole(product.get("currentOwnerRole").asText())
                        .productStatus(product.get("productStatus").asText())
                        .verified(product.get("isVerified").asBoolean())
                        .createdAt(product.get("createdAt").asText())
                        .updatedAt(product.get("updatedAt").asText())
                        .build();

                historyItems.add(historyItem);
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