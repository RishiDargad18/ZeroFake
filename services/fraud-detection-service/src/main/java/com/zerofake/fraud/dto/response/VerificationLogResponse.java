package com.zerofake.fraud.dto.response;

import com.zerofake.fraud.constant.VerificationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationLogResponse {

    private UUID verificationId;

    private UUID productId;

    private Integer riskScore;

    private Boolean authentic;

    private Boolean fraudDetected;

    private VerificationResult verificationResult;

    private LocalDateTime scannedAt;

}