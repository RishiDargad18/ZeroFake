package com.zerofake.product.dto.response;

import com.zerofake.product.constant.BatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchResponse {

    private UUID id;

    private String batchNumber;

    private UUID productId;

    private String productCode;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    private Integer quantityProduced;

    private Integer availableQuantity;

    private String manufacturingLocation;

    private String remarks;

    private BatchStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}