package com.zerofake.fraud.dto.response;

import com.zerofake.fraud.constant.VerificationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private UUID productId;

    private Boolean authentic;

    private Boolean fraudDetected;

    private Integer riskScore;

    private VerificationResult verificationResult;

    private List<String> triggeredRules;

    private String message;

}