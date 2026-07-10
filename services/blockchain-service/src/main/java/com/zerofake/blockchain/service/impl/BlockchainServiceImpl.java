package com.zerofake.blockchain.service.impl;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.dto.request.RegisterProductRequest;
import com.zerofake.blockchain.dto.request.TransferOwnershipRequest;
import com.zerofake.blockchain.dto.request.VerifyProductRequest;
import com.zerofake.blockchain.dto.response.BlockchainTransactionResponse;
import com.zerofake.blockchain.dto.response.ProductHistoryResponse;
import com.zerofake.blockchain.dto.response.VerificationResponse;
import com.zerofake.blockchain.exception.ResourceNotFoundException;
import com.zerofake.blockchain.mapper.BlockchainTransactionMapper;
import com.zerofake.blockchain.repository.BlockchainTransactionRepository;
import com.zerofake.blockchain.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockchainServiceImpl implements BlockchainService {

    private final BlockchainTransactionRepository blockchainTransactionRepository;

    private final BlockchainTransactionMapper blockchainTransactionMapper;

    @Override
    public BlockchainTransactionResponse registerProduct(RegisterProductRequest request) {
        throw new UnsupportedOperationException("Will be implemented during Hyperledger Fabric integration.");
    }

    @Override
    public BlockchainTransactionResponse transferOwnership(TransferOwnershipRequest request) {
        throw new UnsupportedOperationException("Will be implemented during Hyperledger Fabric integration.");
    }

    @Override
    public VerificationResponse verifyProduct(VerifyProductRequest request) {
        throw new UnsupportedOperationException("Will be implemented during Hyperledger Fabric integration.");
    }

    @Override
    public ProductHistoryResponse getProductHistory(UUID productId) {
        throw new UnsupportedOperationException("Will be implemented during Hyperledger Fabric integration.");
    }

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

}