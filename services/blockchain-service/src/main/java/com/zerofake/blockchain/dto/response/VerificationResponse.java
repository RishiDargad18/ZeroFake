package com.zerofake.blockchain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The on-chain state of a product, returned by a ledger query.
 *
 * <p>There is no transaction identifier here: verification reads the ledger and
 * commits nothing, so no transaction exists to identify.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationResponse {

    private UUID productId;

    /** True when the product's identity is anchored on the ledger. */
    private Boolean authentic;

    private String message;

    private UUID manufacturerId;

    private UUID currentOwnerId;

    private String currentOwnerRole;

    private String productStatus;

    private String registeredAt;

    private String lastUpdatedAt;

}
