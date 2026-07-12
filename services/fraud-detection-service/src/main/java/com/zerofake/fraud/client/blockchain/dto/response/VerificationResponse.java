package com.zerofake.fraud.client.blockchain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private UUID productId;

    private Boolean authentic;

    private String message;

    private String transactionId;

}