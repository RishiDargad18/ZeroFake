package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.client.blockchain.BlockchainServiceClient;
import com.zerofake.fraud.client.product.ProductServiceClient;
import com.zerofake.fraud.client.product.dto.BatchResponse;
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
import com.zerofake.fraud.security.AuthenticatedUser;
import com.zerofake.fraud.security.SecurityUtils;
import com.zerofake.fraud.service.FraudAssessment;
import com.zerofake.fraud.service.VerificationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Decides whether a scanned product is genuine.
 *
 * <p>The verification is read-only with respect to the supply chain: it reads
 * the catalogue and the ledger and writes only its own audit records. It never
 * transfers ownership. Scanning an item is an act of inspection, not an act of
 * custody, and conflating the two would let anyone take ownership of any
 * product simply by pointing a camera at it.
 */
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationServiceImpl.class);

    /** Roles that hold custody of goods and are therefore subject to the ownership rule. */
    private static final Set<String> CUSTODY_ROLES = Set.of(
            "ROLE_MANUFACTURER", "ROLE_WAREHOUSE", "ROLE_DISTRIBUTOR", "ROLE_RETAILER"
    );

    /** A burst of more scans than this within the window is treated as suspicious. */
    private static final int SUSPICIOUS_SCAN_COUNT = 5;
    private static final int SUSPICIOUS_WINDOW_MINUTES = 10;

    private final ProductServiceClient productServiceClient;
    private final BlockchainServiceClient blockchainServiceClient;
    private final VerificationLogRepository verificationLogRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final FraudReportRepository fraudReportRepository;

    @Value("${zerofake.fraud.risk-threshold}")
    private int riskThreshold;

    @Value("${zerofake.fraud.suspicious-threshold}")
    private int suspiciousThreshold;

    @Override
    @Transactional
    public VerificationResponse verifyProduct(VerifyProductRequest request) {

        // The scanner's identity comes from their access token, never from the
        // request body — otherwise scan attribution and every rule built on it
        // could be forged by editing the payload.
        AuthenticatedUser scanner = SecurityUtils.currentUser()
                .orElseThrow(() -> new IllegalStateException(
                        "Verification requires an authenticated user."));

        FraudAssessment assessment = assess(request, scanner);

        boolean authentic = !assessment.triggeredRules().contains(FraudType.PRODUCT_NOT_FOUND)
                && !assessment.triggeredRules().contains(FraudType.BLOCKCHAIN_MISMATCH);

        boolean fraudDetected = assessment.riskScore() >= riskThreshold;

        VerificationResult result = classify(assessment.riskScore());

        recordScan(request, scanner, authentic);
        recordVerificationLog(request, scanner, assessment, authentic, fraudDetected, result);

        assessment.headlineFinding().ifPresent(finding -> {
            if (fraudDetected) {
                raiseFraudReport(request.getProductId(), scanner, finding, assessment);
            }
        });

        return VerificationResponse.builder()
                .productId(request.getProductId())
                .authentic(authentic)
                .fraudDetected(fraudDetected)
                .riskScore(assessment.riskScore())
                .verificationResult(result)
                .triggeredRules(assessment.triggeredRuleNames())
                .message(describe(result, assessment))
                .build();
    }

    // ------------------------------------------------------------------
    // Rule evaluation
    // ------------------------------------------------------------------

    /**
     * Evaluates every fraud rule against this scan.
     *
     * <p>This is the platform's only risk engine. A second, subtly different
     * copy previously existed and had already drifted out of step with this one.
     */
    private FraudAssessment assess(VerifyProductRequest request, AuthenticatedUser scanner) {

        Set<FraudType> triggered = EnumSet.noneOf(FraudType.class);

        // Rule 1 — the catalogue has never heard of this product.
        if (!existsInCatalogue(request.getProductId())) {
            return FraudAssessment.of(EnumSet.of(FraudType.PRODUCT_NOT_FOUND));
        }

        // Rule 2 — the product exists on paper but has no ledger identity.
        // A counterfeit carrying a copied product code lands here.
        var onChainState = readLedgerState(request.getProductId());

        if (onChainState == null) {
            return FraudAssessment.of(EnumSet.of(FraudType.BLOCKCHAIN_MISMATCH));
        }

        // Rule 3 — a custody role scanning goods it does not hold.
        if (CUSTODY_ROLES.contains(scanner.role())
                && onChainState.getCurrentOwnerId() != null
                && !onChainState.getCurrentOwnerId().equals(scanner.id())) {
            triggered.add(FraudType.INVALID_OWNER);
        }

        List<ScanHistory> previousScans =
                scanHistoryRepository.findByProductIdAndSuccessfulTrue(request.getProductId());

        // Rules 4 and 5 — the same code seen by another party, or in another place.
        if (!previousScans.isEmpty()) {

            boolean seenByAnotherUser = previousScans.stream()
                    .anyMatch(scan -> !scanner.id().equals(scan.getUserId()));

            if (seenByAnotherUser) {
                triggered.add(FraudType.DUPLICATE_QR);
            }

            boolean seenElsewhere = previousScans.stream()
                    .map(ScanHistory::getLocation)
                    .filter(java.util.Objects::nonNull)
                    .anyMatch(location -> !location.equalsIgnoreCase(request.getLocation()));

            if (seenElsewhere) {
                triggered.add(FraudType.MULTIPLE_LOCATION_SCAN);
            }
        }

        // Rule 6 — an implausible burst of scans.
        long recentScans = previousScans.stream()
                .filter(scan -> scan.getScannedAt() != null)
                .filter(scan -> scan.getScannedAt()
                        .isAfter(LocalDateTime.now().minusMinutes(SUSPICIOUS_WINDOW_MINUTES)))
                .count();

        if (recentScans >= SUSPICIOUS_SCAN_COUNT) {
            triggered.add(FraudType.SUSPICIOUS_ACTIVITY);
        }

        // Rule 7 — every batch of this product is past its expiry date.
        if (allBatchesExpired(request.getProductId())) {
            triggered.add(FraudType.EXPIRED_PRODUCT);
        }

        return FraudAssessment.of(triggered);
    }

    /**
     * @return false when the catalogue reports the product does not exist —
     *         the strongest possible counterfeit signal
     */
    private boolean existsInCatalogue(UUID productId) {

        try {
            productServiceClient.getProductById(productId);
            return true;

        } catch (FeignException.NotFound ex) {
            return false;

        } catch (FeignException ex) {
            throw new ExternalServiceException(
                    "The product catalogue is unavailable, so authenticity cannot be confirmed."
            );
        }
    }

    /**
     * Reads the product's state from the ledger.
     *
     * <p>Returns null when the product has no ledger identity. That is a
     * business outcome — the definition of a blockchain mismatch — and must not
     * be allowed to surface as a server error, which is what previously turned
     * every detected counterfeit into an HTTP 500.
     */
    private com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse readLedgerState(
            UUID productId
    ) {

        var blockchainRequest =
                com.zerofake.fraud.client.blockchain.dto.request.VerifyProductRequest
                        .builder()
                        .productId(productId)
                        .build();

        try {
            var wrapper = blockchainServiceClient.verifyProduct(blockchainRequest);

            var state = wrapper == null ? null : wrapper.getData();

            if (state == null || !Boolean.TRUE.equals(state.getAuthentic())) {
                return null;
            }

            return state;

        } catch (FeignException.NotFound ex) {
            return null;

        } catch (FeignException ex) {
            throw new ExternalServiceException(
                    "The blockchain service is unavailable, so authenticity cannot be confirmed."
            );
        }
    }

    private boolean allBatchesExpired(UUID productId) {

        try {
            var wrapper = productServiceClient.getBatchesByProduct(productId);

            List<BatchResponse> batches = wrapper == null ? null : wrapper.getData();

            if (batches == null || batches.isEmpty()) {
                return false;
            }

            LocalDate today = LocalDate.now();

            List<BatchResponse> datedBatches = batches.stream()
                    .filter(batch -> batch.getExpiryDate() != null)
                    .toList();

            return !datedBatches.isEmpty()
                    && datedBatches.stream()
                    .allMatch(batch -> batch.getExpiryDate().isBefore(today));

        } catch (FeignException ex) {
            // Expiry is a secondary signal. If the catalogue cannot answer, the
            // verification still stands on the checks that did complete.
            log.warn("Could not read batches for product {}: {}", productId, ex.getMessage());
            return false;
        }
    }

    private VerificationResult classify(int riskScore) {

        if (riskScore >= riskThreshold) {
            return VerificationResult.COUNTERFEIT;
        }

        if (riskScore >= suspiciousThreshold) {
            return VerificationResult.SUSPICIOUS;
        }

        return VerificationResult.GENUINE;
    }

    private String describe(VerificationResult result, FraudAssessment assessment) {

        return switch (result) {
            case GENUINE -> "Product verified as genuine.";
            case SUSPICIOUS -> "Product is authentic but its scan history is unusual: "
                    + String.join(", ", assessment.triggeredRuleNames()) + ".";
            case COUNTERFEIT -> assessment.headlineFinding()
                    .map(finding -> switch (finding) {
                        case PRODUCT_NOT_FOUND ->
                                "This product does not exist in the ZeroFake catalogue. "
                                        + "It is almost certainly counterfeit.";
                        case BLOCKCHAIN_MISMATCH ->
                                "This product has no blockchain identity. "
                                        + "Its authenticity cannot be established.";
                        default -> "Fraud indicators detected: "
                                + String.join(", ", assessment.triggeredRuleNames()) + ".";
                    })
                    .orElse("Fraud detected during verification.");
        };
    }

    // ------------------------------------------------------------------
    // Audit records
    // ------------------------------------------------------------------

    private void recordScan(
            VerifyProductRequest request,
            AuthenticatedUser scanner,
            boolean authentic
    ) {

        scanHistoryRepository.save(
                ScanHistory.builder()
                        .productId(request.getProductId())
                        .userId(scanner.id())
                        .userRole(scanner.role())
                        .ipAddress(request.getIpAddress())
                        .location(request.getLocation())
                        .deviceInfo(request.getDeviceInfo())
                        .successful(authentic)
                        .scannedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void recordVerificationLog(
            VerifyProductRequest request,
            AuthenticatedUser scanner,
            FraudAssessment assessment,
            boolean authentic,
            boolean fraudDetected,
            VerificationResult result
    ) {

        verificationLogRepository.save(
                VerificationLog.builder()
                        .productId(request.getProductId())
                        .scannedByUserId(scanner.id())
                        .scannedByRole(scanner.role())
                        .authentic(authentic)
                        .fraudDetected(fraudDetected)
                        .riskScore(assessment.riskScore())
                        .verificationResult(result)
                        .remarks(assessment.triggeredRules().isEmpty()
                                ? "No fraud rules triggered."
                                : String.join(", ", assessment.triggeredRuleNames()))
                        .ipAddress(request.getIpAddress())
                        .deviceInfo(request.getDeviceInfo())
                        .location(request.getLocation())
                        .scannedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void raiseFraudReport(
            UUID productId,
            AuthenticatedUser scanner,
            FraudType finding,
            FraudAssessment assessment
    ) {

        fraudReportRepository.save(
                FraudReport.builder()
                        .productId(productId)
                        .reportedByUserId(scanner.id())
                        .fraudType(finding)
                        .riskScore(assessment.riskScore())
                        .description("Automatically raised by verification. Rules triggered: "
                                + String.join(", ", assessment.triggeredRuleNames()) + ".")
                        .status(FraudStatus.OPEN)
                        .detectedAt(LocalDateTime.now())
                        .build()
        );

        log.warn(
                "Fraud report raised for product {} by user {} — {} (risk {})",
                productId, scanner.id(), finding, assessment.riskScore()
        );
    }
}
