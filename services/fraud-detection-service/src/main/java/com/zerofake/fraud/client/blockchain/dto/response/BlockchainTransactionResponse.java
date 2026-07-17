package com.zerofake.fraud.client.blockchain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String message;
}
