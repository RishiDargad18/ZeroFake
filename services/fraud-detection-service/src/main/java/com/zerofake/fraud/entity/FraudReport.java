package com.zerofake.fraud.entity;

import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.constant.FraudType;
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
@Table(name = "fraud_reports")
public class FraudReport extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private UUID productId;

    @NotNull
    @Column(nullable = false)
    private UUID reportedByUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudType fraudType;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer riskScore;

    @NotNull
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime detectedAt;

}