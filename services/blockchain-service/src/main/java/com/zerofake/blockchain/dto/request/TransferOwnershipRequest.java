package com.zerofake.blockchain.dto.request;

import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Product ID is required.")
    private UUID productId;

    @NotNull(message = "Current owner ID is required.")
    private UUID fromOwnerId;

    @NotNull(message = "New owner ID is required.")
    private UUID toOwnerId;

}