package com.zerofake.product.dto.response;

import com.zerofake.product.constant.BlockchainStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;

    private String productCode;

    private String productName;

    private String description;

    private String brand;

    private UUID manufacturerId;

    private CategoryResponse category;

    private String qrCodePath;

    private String imageUrl;

    private String imageFileName;

    private String imageContentType;

    private Long imageSize;

    private Boolean active;

    private BlockchainStatus blockchainStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}