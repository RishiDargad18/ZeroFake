package com.zerofake.blockchain.dto.response;

import com.zerofake.blockchain.constant.BlockchainStatus;
import com.zerofake.blockchain.constant.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockchainTransactionResponse {

    private UUID id;

    private UUID productId;

    private String transactionId;

    private Long blockNumber;

    private String blockHash;

    private TransactionType transactionType;

    private UUID performedBy;

    private BlockchainStatus status;

    private String message;

    private LocalDateTime createdAt;

}