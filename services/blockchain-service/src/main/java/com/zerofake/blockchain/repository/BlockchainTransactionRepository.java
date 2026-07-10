package com.zerofake.blockchain.repository;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import com.zerofake.blockchain.entity.BlockchainTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, UUID> {

    Optional<BlockchainTransaction> findByTransactionId(String transactionId);

    List<BlockchainTransaction> findByProductId(UUID productId);

    List<BlockchainTransaction> findByProductIdOrderByCreatedAtAsc(UUID productId);

    List<BlockchainTransaction> findByTransactionType(TransactionType transactionType);

    List<BlockchainTransaction> findByStatus(BlockchainStatus status);

    boolean existsByTransactionId(String transactionId);

}