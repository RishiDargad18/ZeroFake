package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.client.blockchain.BlockchainServiceClient;
import com.zerofake.fraud.client.product.ProductServiceClient;
import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.constant.FraudType;
import com.zerofake.fraud.constant.VerificationResult;
import com.zerofake.fraud.dto.request.VerifyProductRequest;
import com.zerofake.fraud.dto.response.VerificationResponse;
import com.zerofake.fraud.entity.FraudReport;
import com.zerofake.fraud.entity.ScanHistory;
import com.zerofake.fraud.entity.VerificationLog;
import com.zerofake.fraud.exception.ExternalServiceException;
import com.zerofake.fraud.repository.FraudReportRepository;
import com.zerofake.fraud.repository.ScanHistoryRepository;
import com.zerofake.fraud.repository.VerificationLogRepository;
import com.zerofake.fraud.service.VerificationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final ProductServiceClient productServiceClient;

    private final BlockchainServiceClient blockchainServiceClient;

    private final VerificationLogRepository verificationLogRepository;

    private final ScanHistoryRepository scanHistoryRepository;

    private final FraudReportRepository fraudReportRepository;

    @Override
    public Integer calculateRiskScore(VerifyProductRequest request) {

        int riskScore = 0;

        try {

            productServiceClient.getProductById(request.getProductId());

        } catch (FeignException.NotFound exception) {

            riskScore += 100;

            return Math.min(riskScore, 100);

        } catch (FeignException exception) {

            throw new ExternalServiceException(exception.getMessage());
        }

        com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest blockchainRequest =
                com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest
                        .builder()
                        .productId(request.getProductId())
                        .build();

        com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse blockchainResponse =
                blockchainServiceClient.verifyProduct(blockchainRequest);

        if (!Boolean.TRUE.equals(blockchainResponse.getAuthentic())) {

            riskScore += 100;

            return Math.min(riskScore, 100);
        }

        List<ScanHistory> successfulScans = scanHistoryRepository.findByProductId(request.getProductId()).stream()
                .filter(ScanHistory::getSuccessful)
                .toList();

        ScanHistory firstSuccessfulScan = successfulScans.stream()
                .min(java.util.Comparator.comparing(ScanHistory::getScannedAt))
                .orElse(null);

        boolean isFirstVerificationUser = false;
        java.util.UUID firstVerificationUserId = null;
        if (firstSuccessfulScan == null) {
            isFirstVerificationUser = true;
        } else {
            firstVerificationUserId = firstSuccessfulScan.getUserId();
            if (request.getUserId().equals(firstVerificationUserId)) {
                isFirstVerificationUser = true;
            }
        }

        if (!isFirstVerificationUser) {
            java.util.Set<java.util.UUID> otherUserIds = new java.util.HashSet<>();
            for (ScanHistory scan : successfulScans) {
                if (scan.getUserId() != null && !scan.getUserId().equals(firstVerificationUserId)) {
                    otherUserIds.add(scan.getUserId());
                }
            }
            otherUserIds.add(request.getUserId());

            int otherUserCount = otherUserIds.size();
            riskScore += 30 * otherUserCount;

            boolean multipleLocations = successfulScans.stream()
                    .anyMatch(scan -> !scan.getLocation().equalsIgnoreCase(request.getLocation()));

            if (multipleLocations) {
                riskScore += 35;
            }
        }

        return Math.min(riskScore, 100);
    }

    @Override
    public VerificationResponse verifyProduct(VerifyProductRequest request) {

        int riskScore = 0;

        boolean authentic = true;

        boolean fraudDetected = false;

        List<String> triggeredRules = new ArrayList<>();

        FraudType highestFraudType = null;

        try {

            productServiceClient.getProductById(request.getProductId());

        } catch (FeignException.NotFound exception) {

            authentic = false;

            riskScore = 100;

            fraudDetected = true;

            highestFraudType = FraudType.PRODUCT_NOT_FOUND;

            triggeredRules.add(FraudType.PRODUCT_NOT_FOUND.name());

        } catch (FeignException exception) {

            throw new ExternalServiceException(exception.getMessage());

        }

        if (highestFraudType == null) {

            com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest blockchainRequest =
                    com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest
                            .builder()
                            .productId(request.getProductId())
                            .build();

            com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse blockchainResponse =
                    blockchainServiceClient.verifyProduct(blockchainRequest);

            authentic = Boolean.TRUE.equals(blockchainResponse.getAuthentic());

            if (!authentic) {

                riskScore = Math.min(riskScore + 100, 100);

                fraudDetected = true;

                highestFraudType = FraudType.BLOCKCHAIN_MISMATCH;

                triggeredRules.add(FraudType.BLOCKCHAIN_MISMATCH.name());
            }

            // Retrieve blockchain history as per approved workflow.
            blockchainServiceClient.getProductHistory(request.getProductId());

            List<ScanHistory> successfulScans = scanHistoryRepository.findByProductId(request.getProductId()).stream()
                    .filter(ScanHistory::getSuccessful)
                    .toList();

            ScanHistory firstSuccessfulScan = successfulScans.stream()
                    .min(java.util.Comparator.comparing(ScanHistory::getScannedAt))
                    .orElse(null);

            boolean isFirstVerificationUser = false;
            java.util.UUID firstVerificationUserId = null;
            if (firstSuccessfulScan == null) {
                isFirstVerificationUser = true;
            } else {
                firstVerificationUserId = firstSuccessfulScan.getUserId();
                if (request.getUserId().equals(firstVerificationUserId)) {
                    isFirstVerificationUser = true;
                }
            }

            if (!isFirstVerificationUser) {
                java.util.Set<java.util.UUID> otherUserIds = new java.util.HashSet<>();
                for (ScanHistory scan : successfulScans) {
                    if (scan.getUserId() != null && !scan.getUserId().equals(firstVerificationUserId)) {
                        otherUserIds.add(scan.getUserId());
                    }
                }
                otherUserIds.add(request.getUserId());

                int otherUserCount = otherUserIds.size();
                riskScore += 30 * otherUserCount;
                triggeredRules.add(FraudType.DUPLICATE_QR.name());

                boolean multipleLocations = successfulScans.stream()
                        .anyMatch(scan -> !scan.getLocation().equalsIgnoreCase(request.getLocation()));

                if (multipleLocations) {
                    triggeredRules.add(FraudType.MULTIPLE_LOCATION_SCAN.name());
                }

                if (triggeredRules.contains(FraudType.DUPLICATE_QR.name())) {
                    if (highestFraudType == null) {
                        highestFraudType = FraudType.DUPLICATE_QR;
                    }
                }

                if (triggeredRules.contains(FraudType.MULTIPLE_LOCATION_SCAN.name())) {
                    if (highestFraudType == null || highestFraudType == FraudType.DUPLICATE_QR) {
                        highestFraudType = FraudType.MULTIPLE_LOCATION_SCAN;
                    }
                }
            }

            riskScore = Math.min(riskScore, 100);

            fraudDetected = riskScore >= 80;
        }

        VerificationResult verificationResult;

        if (riskScore >= 80) {
            verificationResult = VerificationResult.COUNTERFEIT;
        } else if (riskScore >= 20) {
            verificationResult = VerificationResult.SUSPICIOUS;
        } else {
            verificationResult = VerificationResult.GENUINE;
        }
        ScanHistory scanHistory = ScanHistory.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .userRole(request.getUserRole())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .deviceInfo(request.getDeviceInfo())
                .successful(authentic)
                .scannedAt(LocalDateTime.now())
                .build();

        scanHistoryRepository.save(scanHistory);

        VerificationLog verificationLog = VerificationLog.builder()
                .productId(request.getProductId())
                .scannedByUserId(request.getUserId())
                .scannedByRole(request.getUserRole())
                .authentic(authentic)
                .fraudDetected(fraudDetected)
                .riskScore(riskScore)
                .verificationResult(verificationResult)
                .remarks(triggeredRules.isEmpty()
                        ? "No fraud rules triggered."
                        : String.join(", ", triggeredRules))
                .ipAddress(request.getIpAddress())
                .deviceInfo(request.getDeviceInfo())
                .location(request.getLocation())
                .scannedAt(LocalDateTime.now())
                .build();

        verificationLogRepository.save(verificationLog);

        if (fraudDetected && highestFraudType != null) {

            FraudReport fraudReport = FraudReport.builder()
                    .productId(request.getProductId())
                    .reportedByUserId(request.getUserId())
                    .fraudType(highestFraudType)
                    .riskScore(riskScore)
                    .description("Fraud detected by rule: " + highestFraudType.name())
                    .status(FraudStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build();

            fraudReportRepository.save(fraudReport);
        }

        return VerificationResponse.builder()
                .productId(request.getProductId())
                .authentic(authentic)
                .fraudDetected(fraudDetected)
                .riskScore(riskScore)
                .verificationResult(verificationResult)
                .triggeredRules(triggeredRules)
                .message(fraudDetected
                        ? "Fraud detected during verification."
                        : "Product verified successfully.")
                .build();
    }
}