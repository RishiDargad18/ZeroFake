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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Fraud Detection",
        description = "Product verification, fraud reports, verification logs and scan history"
)
public class FraudDetectionController {

    private final VerificationService verificationService;
    private final FraudReportService fraudReportService;
    private final VerificationLogService verificationLogService;
    private final ScanHistoryService scanHistoryService;

    @Operation(
            summary = "Verify product authenticity",
            description = "Runs every fraud rule against the scan and returns a verdict. "
                    + "The scanning user is taken from the access token."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "A service required for verification is unavailable")
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyProduct(
            @Valid @RequestBody VerifyProductRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Product verification completed.",
                verificationService.verifyProduct(request)
        ));
    }

    @Operation(summary = "Raise a fraud report")
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<FraudReportResponse>> createFraudReport(
            @Valid @RequestBody FraudReportRequest request) {

        FraudReportResponse response = fraudReportService.createFraudReport(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Fraud report created successfully.",
                        response
                ));
    }

    @Operation(summary = "Get all fraud reports")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<FraudReportResponse>>> getAllFraudReports() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Fraud reports retrieved successfully.",
                fraudReportService.getAllFraudReports()
        ));
    }

    @Operation(summary = "Get a fraud report by ID")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<FraudReportResponse>> getFraudReportById(
            @PathVariable UUID reportId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Fraud report retrieved successfully.",
                fraudReportService.getFraudReportById(reportId)
        ));
    }

    @Operation(summary = "Get all verification logs")
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<VerificationLogResponse>>> getAllVerificationLogs() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Verification logs retrieved successfully.",
                verificationLogService.getAllVerificationLogs()
        ));
    }

    @Operation(summary = "Get verification logs for a product")
    @GetMapping("/logs/product/{productId}")
    public ResponseEntity<ApiResponse<List<VerificationLogResponse>>> getVerificationLogsByProductId(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Verification logs retrieved successfully.",
                verificationLogService.getVerificationLogsByProductId(productId)
        ));
    }

    @Operation(summary = "Get all scan history")
    @GetMapping("/scans")
    public ResponseEntity<ApiResponse<List<ScanHistoryResponse>>> getAllScanHistory() {

        return ResponseEntity.ok(ApiResponse.ok(
                "Scan history retrieved successfully.",
                scanHistoryService.getAllScanHistory()
        ));
    }

    @Operation(summary = "Get scan history for a product")
    @GetMapping("/scans/product/{productId}")
    public ResponseEntity<ApiResponse<List<ScanHistoryResponse>>> getScanHistoryByProductId(
            @PathVariable UUID productId) {

        return ResponseEntity.ok(ApiResponse.ok(
                "Scan history retrieved successfully.",
                scanHistoryService.getScanHistoryByProductId(productId)
        ));
    }
}
