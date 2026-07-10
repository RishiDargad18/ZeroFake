package com.zerofake.blockchain.service;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;

import java.util.List;
import java.util.UUID;

public interface BlockchainService {

    BlockchainTransactionResponse registerProduct(RegisterProductRequest request);

    BlockchainTransactionResponse transferOwnership(TransferOwnershipRequest request);

    VerificationResponse verifyProduct(VerifyProductRequest request);

    ProductHistoryResponse getProductHistory(UUID productId);

    BlockchainTransactionResponse getTransactionByTransactionId(String transactionId);

    List<BlockchainTransactionResponse> getTransactionsByProductId(UUID productId);

    List<BlockchainTransactionResponse> getTransactionsByStatus(BlockchainStatus status);

    List<BlockchainTransactionResponse> getTransactionsByTransactionType(TransactionType transactionType);

}