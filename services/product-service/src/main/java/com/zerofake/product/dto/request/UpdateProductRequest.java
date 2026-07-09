package com.zerofake.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UpdateProductRequest {

    @NotBlank
    @Size(max = 100)
    private String productCode;

    @NotBlank
    @Size(max = 255)
    private String productName;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 150)
    private String brand;

    @NotNull
    private UUID manufacturerId;

    @NotNull
    private UUID categoryId;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 255)
    private String imageFileName;

    @Size(max = 100)
    private String imageContentType;

    private Long imageSize;

}