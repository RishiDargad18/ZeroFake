package com.zerofake.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBatchRequest {

    @NotBlank
    @Size(max = 100)
    private String batchNumber;

    @NotNull
    private UUID productId;

    @NotNull
    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    @NotNull
    @Positive
    private Integer quantityProduced;

    @NotNull
    @PositiveOrZero
    private Integer availableQuantity;

    @Size(max = 255)
    private String manufacturingLocation;

    @Size(max = 500)
    private String remarks;

}