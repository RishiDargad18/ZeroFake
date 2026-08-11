package com.zerofake.fraud.client.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The subset of a manufacturing batch this service needs to detect expired
 * stock in circulation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {

    private UUID id;

    private String batchNumber;

    private UUID productId;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    private String status;
}
