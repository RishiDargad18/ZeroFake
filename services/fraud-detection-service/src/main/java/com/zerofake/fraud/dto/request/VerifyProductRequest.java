package com.zerofake.fraud.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VerifyProductRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID userId;

    @NotBlank
    private String userRole;

    @NotBlank
    private String ipAddress;

    @NotBlank
    private String deviceInfo;

    @NotBlank
    private String location;

}