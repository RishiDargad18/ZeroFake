package com.zerofake.fraud.client.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The subset of the product catalogue record this service needs.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;

    private String productCode;

    private String productName;

    private String brand;

    private UUID manufacturerId;

    private Boolean active;

    private String blockchainStatus;
}
