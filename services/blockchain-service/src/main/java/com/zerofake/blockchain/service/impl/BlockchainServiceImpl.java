package com.zerofake.blockchain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofake.blockchain.client.ProductServiceClient;
import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryItemResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;
import com.zerofake.blockchain.entity.BlockchainTransaction;
import com.zerofake.blockchain.exception.BlockchainOperationException;
import com.zerofake.blockchain.exception.ConflictException;
import com.zerofake.blockchain.exception.ResourceNotFoundException;
import com.zerofake.blockchain.fabric.ChaincodeErrors;
import com.zerofake.blockchain.fabric.FabricContractService;
import com.zerofake.blockchain.mapper.BlockchainTransactionMapper;
import com.zerofake.blockchain.repository.BlockchainTransactionRepository;
import com.zerofake.blockchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.client.Proposal;
import org.hyperledger.fabric.client.Status;
import org.hyperledger.fabric.client.SubmittedTransaction;
import org.hyperledger.fabric.client.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    private static final String REGISTER_PRODUCT = "RegisterProduct";
    private static final String TRANSFER_OWNERSHIP = "TransferOwnership";
    private static final String VERIFY_PRODUCT = "VerifyProduct";
    private static final String GET_PRODUCT_HISTORY = "GetProductHistory";

    private final BlockchainTransactionRepository blockchainTransactionRepository;
    private final BlockchainTransactionMapper blockchainTransactionMapper;
    private final FabricContractService fabricContractService;
    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // Ledger writes
    // ------------------------------------------------------------------

    /**
     * Anchors a product's identity on the ledger.
     *
     * <p>Registration is rejected outright if the product already has a
     * successful registration transaction. Previously this condition was
     * swallowed and recorded as a synthetic "success", which put transaction
     * identifiers into the audit trail that existed nowhere on the ledger.
     */
    @Override
    public BlockchainTransactionResponse registerProduct(RegisterProductRequest request) {

        blockchainTransactionRepository
                .findFirstByProductIdAndTransactionTypeAndStatus(
                        request.getProductId(),
                        TransactionType.PRODUCT_REGISTERED,
                        BlockchainStatus.SUCCESS
                )
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Product " + request.getProductId()
                                    + " is already registered on the blockchain "
                                    + "(transaction " + existing.getTransactionId() + ")."
                    );
                });

        BlockchainTransaction transaction = submit(
                REGISTER_PRODUCT,
                TransactionType.PRODUCT_REGISTERED,
                request.getProductId(),
                request.getManufacturerId(),
                "Product registered on the blockchain.",
                new String[]{
                        request.getProductId().toString(),
                        request.getManufacturerId().toString()
                }
        );

        productServiceClient.markRegistered(request.getProductId());

        return blockchainTransactionMapper.toResponse(transaction);
    }

    @Override
    public BlockchainTransactionResponse transferOwnership(TransferOwnershipRequest request) {

        BlockchainTransaction transaction = submit(
                TRANSFER_OWNERSHIP,
                TransactionType.OWNERSHIP_TRANSFERRED,
                request.getProductId(),
                request.getFromOwnerId(),
                "Ownership transferred to " + request.getToOwnerRole() + ".",
                new String[]{
                        request.getProductId().toString(),
                        request.getFromOwnerId().toString(),
                        request.getToOwnerId().toString(),
                        request.getToOwnerRole().name()
                }
        );

        return blockchainTransactionMapper.toResponse(transaction);
    }

    /**
     * Submits a chaincode transaction and records the outcome truthfully.
     *
     * <p>The Fabric transaction identifier is taken from the proposal, so it is
     * known before the transaction is sent and can be recorded even when the
     * submission fails. The block number is read from the commit status.
     */
    private BlockchainTransaction submit(
            String transactionName,
            TransactionType transactionType,
            UUID productId,
            UUID performedBy,
            String successMessage,
            String[] arguments
    ) {

        Proposal proposal = fabricContractService
                .newProposal(transactionName)
                .addArguments(arguments)
                .build();

        String transactionId = proposal.getTransactionId();

        try {
            Transaction transaction = proposal.endorse();

            SubmittedTransaction submitted = transaction.submitAsync();

            // Blocks until the transaction is committed to a block.
            Status status = submitted.getStatus();

            if (!status.isSuccessful()) {
                throw new BlockchainOperationException(
                        "Transaction " + transactionId + " was rejected at commit with code "
                                + status.getCode() + "."
                );
            }

            log.info(
                    "{} committed for product {} in block {} (tx {})",
                    transactionName, productId, status.getBlockNumber(), transactionId
            );

            return record(
                    productId,
                    transactionId,
                    status.getBlockNumber(),
                    transactionType,
                    performedBy,
                    BlockchainStatus.SUCCESS,
                    successMessage
            );

        } catch (Exception ex) {

            String reason = ChaincodeErrors.describe(ex);

            record(
                    productId,
                    transactionId,
                    null,
                    transactionType,
                    performedBy,
                    BlockchainStatus.FAILED,
                    truncate(reason)
            );

            log.warn("{} failed for product {}: {}", transactionName, productId, reason);

            throw translate(ex, transactionName, productId);
        }
    }

    private RuntimeException translate(Exception ex, String transactionName, UUID productId) {

        if (ex instanceof BlockchainOperationException blockchainOperationException) {
            return blockchainOperationException;
        }

        if (ChaincodeErrors.isNotFound(ex)) {
            return new ResourceNotFoundException(
                    "Product " + productId + " is not registered on the blockchain."
            );
        }

        if (ChaincodeErrors.isAlreadyExists(ex)) {
            return new ConflictException(
                    "Product " + productId + " is already registered on the blockchain."
            );
        }

        return new BlockchainOperationException(
                transactionName + " failed on the blockchain: " + ChaincodeErrors.describe(ex),
                ex
        );
    }

    /**
     * Persists an audit record.
     *
     * <p>Deliberately not wrapped in an enclosing transaction: a FAILED record
     * must survive the exception that is about to be thrown, so each record is
     * committed on its own.
     */
    private BlockchainTransaction record(
            UUID productId,
            String transactionId,
            Long blockNumber,
            TransactionType transactionType,
            UUID performedBy,
            BlockchainStatus status,
            String message
    ) {

        return blockchainTransactionRepository.save(
                BlockchainTransaction.builder()
                        .productId(productId)
                        .transactionId(transactionId)
                        .blockNumber(blockNumber)
                        .transactionType(transactionType)
                        .performedBy(performedBy)
                        .status(status)
                        .message(message)
                        .build()
        );
    }

    private String truncate(String value) {

        if (value == null) {
            return null;
        }

        return value.length() <= 500 ? value : value.substring(0, 497) + "...";
    }

    // ------------------------------------------------------------------
    // Ledger queries
    // ------------------------------------------------------------------

    /**
     * Reads a product's current state from the ledger.
     *
     * <p>A product that is absent from the ledger is reported as 404 rather than
     * as a server error, so that the fraud detection service can treat it as the
     * counterfeit signal it is.
     */
    @Override
    public VerificationResponse verifyProduct(VerifyProductRequest request) {

        JsonNode asset = evaluate(
                VERIFY_PRODUCT,
                request.getProductId(),
                request.getProductId().toString()
        );

        return VerificationResponse.builder()
                .productId(request.getProductId())
                .authentic(true)
                .message("Product identity confirmed on the blockchain.")
                .manufacturerId(readUuid(asset, "manufacturerId"))
                .currentOwnerId(readUuid(asset, "currentOwnerId"))
                .currentOwnerRole(readText(asset, "currentOwnerRole"))
                .productStatus(readText(asset, "productStatus"))
                .registeredAt(readText(asset, "createdAt"))
                .lastUpdatedAt(readText(asset, "updatedAt"))
                .build();
    }

    @Override
    public ProductHistoryResponse getProductHistory(UUID productId) {

        JsonNode historyArray = evaluate(
                GET_PRODUCT_HISTORY,
                productId,
                productId.toString()
        );

        List<ProductHistoryItemResponse> historyItems = new ArrayList<>();

        if (historyArray != null && historyArray.isArray()) {
            for (JsonNode entry : historyArray) {
                historyItems.add(
                        ProductHistoryItemResponse.builder()
                                .manufacturerId(readUuid(entry, "manufacturerId"))
                                .currentOwnerId(readUuid(entry, "currentOwnerId"))
                                .currentOwnerRole(readText(entry, "currentOwnerRole"))
                                .productStatus(readText(entry, "productStatus"))
                                .verified(entry.path("isVerified").asBoolean(false))
                                .createdAt(readText(entry, "createdAt"))
                                .updatedAt(readText(entry, "updatedAt"))
                                .build()
                );
            }
        }

        return ProductHistoryResponse.builder()
                .productId(productId)
                .history(historyItems)
                .build();
    }

    private JsonNode evaluate(String transactionName, UUID productId, String... arguments) {

        try {
            byte[] result = fabricContractService
                    .getContract()
                    .evaluateTransaction(transactionName, arguments);

            return objectMapper.readTree(result);

        } catch (Exception ex) {

            if (ChaincodeErrors.isNotFound(ex)) {
                throw new ResourceNotFoundException(
                        "Product " + productId + " is not registered on the blockchain."
                );
            }

            log.warn("{} failed for product {}: {}", transactionName, productId,
                    ChaincodeErrors.describe(ex));

            throw new BlockchainOperationException(
                    transactionName + " failed on the blockchain: " + ChaincodeErrors.describe(ex),
                    ex
            );
        }
    }

    private UUID readUuid(JsonNode node, String field) {

        String value = readText(node, field);

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            // The ledger may hold identifiers written by other tooling that are
            // not UUIDs; surface null rather than failing the whole read.
            log.debug("Ledger field {} is not a UUID: {}", field, value);
            return null;
        }
    }

    private String readText(JsonNode node, String field) {

        JsonNode value = node == null ? null : node.get(field);

        return value == null || value.isNull() ? null : value.asText();
    }

    // ------------------------------------------------------------------
    // Local audit trail
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public BlockchainTransactionResponse getTransactionByTransactionId(String transactionId) {

        return blockchainTransactionMapper.toResponse(
                blockchainTransactionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Blockchain transaction not found with transaction ID: " + transactionId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainTransactionResponse> getTransactionsByProductId(UUID productId) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByProductIdOrderByCreatedAtAsc(productId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainTransactionResponse> getTransactionsByStatus(BlockchainStatus status) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByStatus(status)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainTransactionResponse> getTransactionsByTransactionType(
            TransactionType transactionType
    ) {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findByTransactionType(transactionType)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockchainTransactionResponse> getAllTransactions() {

        return blockchainTransactionMapper.toResponseList(
                blockchainTransactionRepository.findAll()
        );
    }
}
