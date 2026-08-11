package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.client.blockchain.BlockchainServiceClient;
import com.zerofake.fraud.client.common.ApiResponseWrapper;
import com.zerofake.fraud.client.product.ProductServiceClient;
import com.zerofake.fraud.client.product.dto.BatchResponse;
import com.zerofake.fraud.client.product.dto.ProductResponse;
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
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The verification workflow.
 *
 * <p>The behaviour these tests protect is the whole point of the platform: a
 * product that cannot be proven authentic must be *reported* as counterfeit,
 * not crash the request. Before this was fixed, a fake product produced an
 * HTTP 500 and the fraud rule that should have caught it could never fire.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificationServiceImplTest {

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID SCANNER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private BlockchainServiceClient blockchainServiceClient;

    @Mock
    private VerificationLogRepository verificationLogRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @Mock
    private FraudReportRepository fraudReportRepository;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificationService, "riskThreshold", 80);
        ReflectionTestUtils.setField(verificationService, "suspiciousThreshold", 20);

        authenticateAs("ROLE_CUSTOMER");

        // Default happy path: product exists, is on-chain, never scanned before.
        productExists();
        onChainOwnedBy(SCANNER_ID, "CUSTOMER");
        noPriorScans();
        noBatches();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------
    // The core use case
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("counterfeit detection")
    class CounterfeitDetection {

        @Test
        @DisplayName("a product absent from the catalogue is reported COUNTERFEIT, not an error")
        void productMissingFromCatalogueIsCounterfeit() {

            when(productServiceClient.getProductById(PRODUCT_ID))
                    .thenThrow(notFound());

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.COUNTERFEIT);
            assertThat(response.getAuthentic()).isFalse();
            assertThat(response.getFraudDetected()).isTrue();
            assertThat(response.getRiskScore()).isEqualTo(100);
            assertThat(response.getTriggeredRules())
                    .containsExactly(FraudType.PRODUCT_NOT_FOUND.name());
        }

        @Test
        @DisplayName("a product absent from the ledger is reported COUNTERFEIT, not an error")
        void productMissingFromLedgerIsCounterfeit() {

            // This is the regression that mattered most: the blockchain service
            // answers 404, which previously propagated as a 500 and made the
            // BLOCKCHAIN_MISMATCH rule unreachable.
            when(blockchainServiceClient.verifyProduct(any()))
                    .thenThrow(notFound());

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.COUNTERFEIT);
            assertThat(response.getAuthentic()).isFalse();
            assertThat(response.getRiskScore()).isEqualTo(100);
            assertThat(response.getTriggeredRules())
                    .containsExactly(FraudType.BLOCKCHAIN_MISMATCH.name());
        }

        @Test
        @DisplayName("a ledger record flagged not authentic is reported COUNTERFEIT")
        void ledgerRecordNotAuthenticIsCounterfeit() {

            var state = new com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse();
            state.setAuthentic(false);

            when(blockchainServiceClient.verifyProduct(any()))
                    .thenReturn(wrap(state));

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.COUNTERFEIT);
        }

        @Test
        @DisplayName("a counterfeit verdict raises a fraud report")
        void counterfeitVerdictRaisesFraudReport() {

            when(productServiceClient.getProductById(PRODUCT_ID))
                    .thenThrow(notFound());

            verificationService.verifyProduct(request());

            ArgumentCaptor<FraudReport> captor = ArgumentCaptor.forClass(FraudReport.class);
            verify(fraudReportRepository).save(captor.capture());

            FraudReport report = captor.getValue();

            assertThat(report.getFraudType()).isEqualTo(FraudType.PRODUCT_NOT_FOUND);
            assertThat(report.getRiskScore()).isEqualTo(100);
            assertThat(report.getReportedByUserId()).isEqualTo(SCANNER_ID);
            assertThat(report.getProductId()).isEqualTo(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("unavailable dependencies")
    class UnavailableDependencies {

        @Test
        @DisplayName("a catalogue outage is an error, never a counterfeit verdict")
        void catalogueOutageIsAnError() {

            // "We could not check" must never be reported as "we checked and
            // it is fake".
            when(productServiceClient.getProductById(PRODUCT_ID))
                    .thenThrow(serviceUnavailable());

            assertThatThrownBy(() -> verificationService.verifyProduct(request()))
                    .isInstanceOf(ExternalServiceException.class);

            verify(fraudReportRepository, never()).save(any());
        }

        @Test
        @DisplayName("a blockchain outage is an error, never a counterfeit verdict")
        void blockchainOutageIsAnError() {

            when(blockchainServiceClient.verifyProduct(any()))
                    .thenThrow(serviceUnavailable());

            assertThatThrownBy(() -> verificationService.verifyProduct(request()))
                    .isInstanceOf(ExternalServiceException.class);

            verify(fraudReportRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("fraud rules")
    class Rules {

        @Test
        @DisplayName("a first, clean scan is GENUINE")
        void firstCleanScanIsGenuine() {

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.GENUINE);
            assertThat(response.getAuthentic()).isTrue();
            assertThat(response.getFraudDetected()).isFalse();
            assertThat(response.getRiskScore()).isZero();
            assertThat(response.getTriggeredRules()).isEmpty();
        }

        @Test
        @DisplayName("a scan by a second party triggers DUPLICATE_QR")
        void scanBySecondPartyTriggersDuplicateQr() {

            priorScans(scan(OTHER_USER_ID, "Bengaluru"));

            VerificationResponse response = verificationService.verifyProduct(
                    request("Bengaluru")
            );

            assertThat(response.getTriggeredRules())
                    .containsExactly(FraudType.DUPLICATE_QR.name());
            assertThat(response.getRiskScore()).isEqualTo(30);
            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.SUSPICIOUS);
        }

        @Test
        @DisplayName("a scan in a second location contributes its own risk")
        void scanInSecondLocationContributesRisk() {

            // The regression this guards: the location rule was reported but
            // never scored, so two-location fraud scored the same as one.
            priorScans(scan(OTHER_USER_ID, "Bengaluru"));

            VerificationResponse response = verificationService.verifyProduct(
                    request("Mumbai")
            );

            assertThat(response.getTriggeredRules()).containsExactly(
                    FraudType.MULTIPLE_LOCATION_SCAN.name(),
                    FraudType.DUPLICATE_QR.name()
            );
            assertThat(response.getRiskScore()).isEqualTo(65);
        }

        @Test
        @DisplayName("location comparison ignores case")
        void locationComparisonIgnoresCase() {

            priorScans(scan(OTHER_USER_ID, "bengaluru"));

            VerificationResponse response = verificationService.verifyProduct(
                    request("BENGALURU")
            );

            assertThat(response.getTriggeredRules())
                    .doesNotContain(FraudType.MULTIPLE_LOCATION_SCAN.name());
        }

        @Test
        @DisplayName("re-scanning your own item in one place is not fraud")
        void rescanningYourOwnItemIsNotFraud() {

            priorScans(scan(SCANNER_ID, "Bengaluru"), scan(SCANNER_ID, "Bengaluru"));

            VerificationResponse response = verificationService.verifyProduct(
                    request("Bengaluru")
            );

            assertThat(response.getTriggeredRules()).isEmpty();
            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.GENUINE);
        }

        @Test
        @DisplayName("a custody role scanning goods it does not own triggers INVALID_OWNER")
        void custodyRoleScanningUnownedGoodsTriggersInvalidOwner() {

            authenticateAs("ROLE_DISTRIBUTOR");
            onChainOwnedBy(OTHER_USER_ID, "RETAILER");

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getTriggeredRules())
                    .contains(FraudType.INVALID_OWNER.name());
            assertThat(response.getRiskScore()).isEqualTo(40);
        }

        @Test
        @DisplayName("a customer scanning goods they do not own is not INVALID_OWNER")
        void customerScanningUnownedGoodsIsNotInvalidOwner() {

            // A shopper inspecting an item on a shelf holds no custody of it.
            authenticateAs("ROLE_CUSTOMER");
            onChainOwnedBy(OTHER_USER_ID, "RETAILER");

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getTriggeredRules())
                    .doesNotContain(FraudType.INVALID_OWNER.name());
        }

        @Test
        @DisplayName("a burst of recent scans triggers SUSPICIOUS_ACTIVITY")
        void burstOfRecentScansTriggersSuspiciousActivity() {

            ScanHistory[] burst = new ScanHistory[5];
            for (int i = 0; i < burst.length; i++) {
                burst[i] = scan(SCANNER_ID, "Bengaluru", LocalDateTime.now().minusMinutes(2));
            }

            priorScans(burst);

            VerificationResponse response = verificationService.verifyProduct(
                    request("Bengaluru")
            );

            assertThat(response.getTriggeredRules())
                    .contains(FraudType.SUSPICIOUS_ACTIVITY.name());
        }

        @Test
        @DisplayName("old scans do not count towards the burst window")
        void oldScansDoNotCountTowardsBurstWindow() {

            ScanHistory[] old = new ScanHistory[6];
            for (int i = 0; i < old.length; i++) {
                old[i] = scan(SCANNER_ID, "Bengaluru", LocalDateTime.now().minusHours(3));
            }

            priorScans(old);

            VerificationResponse response = verificationService.verifyProduct(
                    request("Bengaluru")
            );

            assertThat(response.getTriggeredRules())
                    .doesNotContain(FraudType.SUSPICIOUS_ACTIVITY.name());
        }

        @Test
        @DisplayName("a product whose every batch has expired triggers EXPIRED_PRODUCT")
        void fullyExpiredProductTriggersExpiredProduct() {

            batches(
                    batch(LocalDate.now().minusDays(30)),
                    batch(LocalDate.now().minusDays(2))
            );

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getTriggeredRules())
                    .contains(FraudType.EXPIRED_PRODUCT.name());
            assertThat(response.getRiskScore()).isEqualTo(25);
        }

        @Test
        @DisplayName("one in-date batch is enough to clear the expiry rule")
        void oneInDateBatchClearsExpiryRule() {

            batches(
                    batch(LocalDate.now().minusDays(30)),
                    batch(LocalDate.now().plusDays(90))
            );

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getTriggeredRules())
                    .doesNotContain(FraudType.EXPIRED_PRODUCT.name());
        }

        @Test
        @DisplayName("a product with no batch expiry data does not trigger the expiry rule")
        void productWithoutExpiryDataDoesNotTriggerExpiryRule() {

            batches(batch(null));

            VerificationResponse response = verificationService.verifyProduct(request());

            assertThat(response.getTriggeredRules())
                    .doesNotContain(FraudType.EXPIRED_PRODUCT.name());
        }
    }

    // ------------------------------------------------------------------
    // Audit trail and boundaries
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("audit records")
    class AuditRecords {

        @Test
        @DisplayName("every verification writes a scan record and a verification log")
        void everyVerificationWritesScanAndLog() {

            verificationService.verifyProduct(request());

            verify(scanHistoryRepository).save(any(ScanHistory.class));
            verify(verificationLogRepository).save(any(VerificationLog.class));
        }

        @Test
        @DisplayName("the scan is attributed to the token holder, never to the request body")
        void scanIsAttributedToTokenHolder() {

            verificationService.verifyProduct(request());

            ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
            verify(scanHistoryRepository).save(captor.capture());

            assertThat(captor.getValue().getUserId()).isEqualTo(SCANNER_ID);
            assertThat(captor.getValue().getUserRole()).isEqualTo("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("a genuine verification raises no fraud report")
        void genuineVerificationRaisesNoFraudReport() {

            verificationService.verifyProduct(request());

            verify(fraudReportRepository, never()).save(any());
        }

        @Test
        @DisplayName("a merely suspicious verification raises no fraud report")
        void suspiciousVerificationRaisesNoFraudReport() {

            priorScans(scan(OTHER_USER_ID, "Bengaluru"));

            VerificationResponse response = verificationService.verifyProduct(
                    request("Bengaluru")
            );

            assertThat(response.getVerificationResult()).isEqualTo(VerificationResult.SUSPICIOUS);
            verify(fraudReportRepository, never()).save(any());
        }

        @Test
        @DisplayName("verification never transfers ownership on the ledger")
        void verificationNeverTransfersOwnership() {

            // Inspection is not custody. The client interface deliberately
            // exposes no write operation, and this asserts the workflow stays
            // read-only: only the two query calls are made.
            verificationService.verifyProduct(request());

            verify(blockchainServiceClient).verifyProduct(any());
            org.mockito.Mockito.verifyNoMoreInteractions(blockchainServiceClient);
        }
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private VerifyProductRequest request() {
        return request("Bengaluru");
    }

    private VerifyProductRequest request(String location) {
        return VerifyProductRequest.builder()
                .productId(PRODUCT_ID)
                .ipAddress("203.0.113.7")
                .deviceInfo("JUnit")
                .location(location)
                .build();
    }

    private void authenticateAs(String role) {
        AuthenticatedUser user =
                new AuthenticatedUser(SCANNER_ID, "scanner@zerofake.com", role);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority(role))
                )
        );
    }

    private void productExists() {
        ProductResponse product = ProductResponse.builder()
                .id(PRODUCT_ID)
                .productName("Test Product")
                .active(true)
                .build();

        when(productServiceClient.getProductById(PRODUCT_ID)).thenReturn(wrap(product));
    }

    private void onChainOwnedBy(UUID ownerId, String ownerRole) {
        var state = new com.zerofake.fraud.client.blockchain.dto.response.VerificationResponse();
        state.setProductId(PRODUCT_ID);
        state.setAuthentic(true);
        state.setCurrentOwnerId(ownerId);
        state.setCurrentOwnerRole(ownerRole);

        when(blockchainServiceClient.verifyProduct(any())).thenReturn(wrap(state));
    }

    private void noPriorScans() {
        when(scanHistoryRepository.findByProductIdAndSuccessfulTrue(PRODUCT_ID))
                .thenReturn(List.of());
    }

    private void priorScans(ScanHistory... scans) {
        when(scanHistoryRepository.findByProductIdAndSuccessfulTrue(PRODUCT_ID))
                .thenReturn(List.of(scans));
    }

    private void noBatches() {
        when(productServiceClient.getBatchesByProduct(PRODUCT_ID))
                .thenReturn(wrap(List.of()));
    }

    private void batches(BatchResponse... batches) {
        when(productServiceClient.getBatchesByProduct(PRODUCT_ID))
                .thenReturn(wrap(List.of(batches)));
    }

    private static ScanHistory scan(UUID userId, String location) {
        return scan(userId, location, LocalDateTime.now().minusDays(1));
    }

    private static ScanHistory scan(UUID userId, String location, LocalDateTime at) {
        return ScanHistory.builder()
                .productId(PRODUCT_ID)
                .userId(userId)
                .userRole("ROLE_CUSTOMER")
                .location(location)
                .ipAddress("203.0.113.9")
                .deviceInfo("JUnit")
                .successful(true)
                .scannedAt(at)
                .build();
    }

    private static BatchResponse batch(LocalDate expiryDate) {
        return BatchResponse.builder()
                .id(UUID.randomUUID())
                .productId(PRODUCT_ID)
                .batchNumber("B-" + UUID.randomUUID())
                .expiryDate(expiryDate)
                .build();
    }

    private static <T> ApiResponseWrapper<T> wrap(T data) {
        ApiResponseWrapper<T> wrapper = new ApiResponseWrapper<>();
        wrapper.setSuccess(true);
        wrapper.setStatus(200);
        wrapper.setData(data);
        return wrapper;
    }

    private static FeignException notFound() {
        return FeignException.errorStatus("GET", feignResponse(404));
    }

    private static FeignException serviceUnavailable() {
        return FeignException.errorStatus("GET", feignResponse(503));
    }

    private static feign.Response feignResponse(int status) {
        return feign.Response.builder()
                .status(status)
                .reason("test")
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "/test",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        new RequestTemplate()
                ))
                .headers(Map.of())
                .build();
    }
}
