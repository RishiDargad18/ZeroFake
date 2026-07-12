package com.zerofake.fraud.dto.response;

import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.constant.FraudType;
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
public class FraudReportResponse {

    private UUID reportId;

    private UUID productId;

    private FraudType fraudType;

    private FraudStatus status;

    private Integer riskScore;

    private LocalDateTime detectedAt;

}