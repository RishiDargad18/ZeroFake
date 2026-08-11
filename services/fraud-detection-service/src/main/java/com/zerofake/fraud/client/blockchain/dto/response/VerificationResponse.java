package com.zerofake.fraud.client.blockchain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The on-chain state of a product as reported by the blockchain service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private UUID productId;

    private Boolean authentic;

    private String message;

    private UUID manufacturerId;

    private UUID currentOwnerId;

    private String currentOwnerRole;

    private String productStatus;

    private String registeredAt;

    private String lastUpdatedAt;
}
