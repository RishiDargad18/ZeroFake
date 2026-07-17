package com.zerofake.fraud.client.blockchain.dto.request;

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
public class TransferOwnershipRequest {

    private UUID productId;

    private UUID fromOwnerId;

    private UUID toOwnerId;

    private String toOwnerRole;
}
