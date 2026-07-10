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
public class RegisterProductRequest {

    @NotNull(message = "Product ID is required.")
    private UUID productId;

    @NotNull(message = "Manufacturer ID is required.")
    private UUID manufacturerId;

}