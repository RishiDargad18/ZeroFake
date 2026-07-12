package com.zerofake.fraud.controller;

import com.zerofake.fraud.dto.common.ApiResponse;
import com.zerofake.fraud.dto.request.FraudReportRequest;
import com.zerofake.fraud.dto.request.VerifyProductRequest;
import com.zerofake.fraud.dto.response.FraudReportResponse;
import com.zerofake.fraud.dto.response.ScanHistoryResponse;
import com.zerofake.fraud.dto.response.VerificationLogResponse;
import com.zerofake.fraud.dto.response.VerificationResponse;
import com.zerofake.fraud.service.FraudReportService;
import com.zerofake.fraud.service.ScanHistoryService;
import com.zerofake.fraud.service.VerificationLogService;
import com.zerofake.fraud.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Validated
@Tag(name = "Fraud Detection", description = "Fraud detection, verification, fraud reports and scan history APIs")
public class FraudDetectionController {

    private final VerificationService verificationService;
    private final FraudReportService fraudReportService;
    private final VerificationLogService verificationLogService;
    private final ScanHistoryService scanHistoryService;

    @Operation(summary = "Verify product authenticity")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyProduct(
            @Valid @RequestBody VerifyProductRequest request) {

        VerificationResponse response = verificationService.verifyProduct(request);

        return ResponseEntity.ok(
                ApiResponse.<VerificationResponse>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product verification completed successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Create fraud report")
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<FraudReportResponse>> createFraudReport(
            @Valid @RequestBody FraudReportRequest request) {

        FraudReportResponse response = fraudReportService.createFraudReport(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<FraudReportResponse>builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.CREATED.value())
                                .success(true)
                                .message("Fraud report created successfully.")
                                .data(response)
                                .build()
                );
    }

    @Operation(summary = "Get all fraud reports")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<FraudReportResponse>>> getAllFraudReports() {

        List<FraudReportResponse> response = fraudReportService.getAllFraudReports();

        return ResponseEntity.ok(
                ApiResponse.<List<FraudReportResponse>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Fraud reports retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Get fraud report by ID")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<FraudReportResponse>> getFraudReportById(
            @PathVariable UUID reportId) {

        FraudReportResponse response = fraudReportService.getFraudReportById(reportId);

        return ResponseEntity.ok(
                ApiResponse.<FraudReportResponse>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Fraud report retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Get all verification logs")
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<VerificationLogResponse>>> getAllVerificationLogs() {

        List<VerificationLogResponse> response = verificationLogService.getAllVerificationLogs();

        return ResponseEntity.ok(
                ApiResponse.<List<VerificationLogResponse>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Verification logs retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Get verification logs by product ID")
    @GetMapping("/logs/product/{productId}")
    public ResponseEntity<ApiResponse<List<VerificationLogResponse>>> getVerificationLogsByProductId(
            @PathVariable UUID productId) {

        List<VerificationLogResponse> response =
                verificationLogService.getVerificationLogsByProductId(productId);

        return ResponseEntity.ok(
                ApiResponse.<List<VerificationLogResponse>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Verification logs retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Get all scan history")
    @GetMapping("/scans")
    public ResponseEntity<ApiResponse<List<ScanHistoryResponse>>> getAllScanHistory() {

        List<ScanHistoryResponse> response = scanHistoryService.getAllScanHistory();

        return ResponseEntity.ok(
                ApiResponse.<List<ScanHistoryResponse>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Scan history retrieved successfully.")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Get scan history by product ID")
    @GetMapping("/scans/product/{productId}")
    public ResponseEntity<ApiResponse<List<ScanHistoryResponse>>> getScanHistoryByProductId(
            @PathVariable UUID productId) {

        List<ScanHistoryResponse> response =
                scanHistoryService.getScanHistoryByProductId(productId);

        return ResponseEntity.ok(
                ApiResponse.<List<ScanHistoryResponse>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Scan history retrieved successfully.")
                        .data(response)
                        .build()
        );
    }
}