package com.zerofake.fraud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "scan_history")
public class ScanHistory extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private UUID productId;

    @NotNull
    @Column(nullable = false)
    private UUID userId;

    @NotNull
    @Column(nullable = false)
    private String userRole;

    @NotNull
    @Column(nullable = false)
    private String ipAddress;

    @NotNull
    @Column(nullable = false)
    private String location;

    @NotNull
    @Column(nullable = false)
    private String deviceInfo;

    @NotNull
    @Column(nullable = false)
    private Boolean successful;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime scannedAt;

}