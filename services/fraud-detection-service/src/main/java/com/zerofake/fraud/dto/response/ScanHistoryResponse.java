package com.zerofake.fraud.dto.response;

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
public class ScanHistoryResponse {

    private UUID scanId;

    private UUID productId;

    private UUID userId;

    private Boolean successful;

    private LocalDateTime scannedAt;

}