package com.zerofake.fraud.entity;

import com.zerofake.fraud.constant.VerificationResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Entity
@Table(name = "verification_logs")
public class VerificationLog extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private UUID productId;

    @NotNull
    @Column(nullable = false)
    private UUID scannedByUserId;

    @NotNull
    @Column(nullable = false)
    private String scannedByRole;

    @NotNull
    @Column(nullable = false)
    private Boolean authentic;

    @NotNull
    @Column(nullable = false)
    private Boolean fraudDetected;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer riskScore;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationResult verificationResult;

    @Column(length = 1000)
    private String remarks;

    @NotNull
    @Column(nullable = false)
    private String ipAddress;

    @NotNull
    @Column(nullable = false)
    private String deviceInfo;

    @NotNull
    @Column(nullable = false)
    private String location;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime scannedAt;

}