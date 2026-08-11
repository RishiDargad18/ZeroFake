package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.constant.FraudType;
import com.zerofake.fraud.dto.request.FraudReportRequest;
import com.zerofake.fraud.dto.response.FraudReportResponse;
import com.zerofake.fraud.entity.FraudReport;
import com.zerofake.fraud.exception.ResourceNotFoundException;
import com.zerofake.fraud.mapper.FraudReportMapper;
import com.zerofake.fraud.repository.FraudReportRepository;
import com.zerofake.fraud.security.AuthenticatedUser;
import com.zerofake.fraud.security.SecurityUtils;
import com.zerofake.fraud.service.FraudReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FraudReportServiceImpl implements FraudReportService {

    /** Risk attributed to a manually raised report until an investigator triages it. */
    private static final int MANUAL_REPORT_RISK_SCORE = 0;

    private final FraudReportRepository fraudReportRepository;
    private final FraudReportMapper fraudReportMapper;

    /**
     * Records a fraud report raised by a user.
     *
     * <p>Every column the entity requires is populated here. This previously
     * omitted the fraud type, risk score and reporter, so the insert failed its
     * not-null constraints and the endpoint could never succeed.
     */
    @Override
    public FraudReportResponse createFraudReport(FraudReportRequest request) {

        AuthenticatedUser reporter = SecurityUtils.currentUser()
                .orElseThrow(() -> new IllegalStateException(
                        "Raising a fraud report requires an authenticated user."));

        FraudType fraudType = request.getFraudType() == null
                ? FraudType.SUSPICIOUS_ACTIVITY
                : request.getFraudType();

        FraudReport fraudReport = FraudReport.builder()
                .productId(request.getProductId())
                .reportedByUserId(reporter.id())
                .fraudType(fraudType)
                .riskScore(MANUAL_REPORT_RISK_SCORE)
                .description(request.getDescription())
                .status(FraudStatus.OPEN)
                .detectedAt(LocalDateTime.now())
                .build();

        return fraudReportMapper.toResponse(fraudReportRepository.save(fraudReport));
    }

    @Override
    @Transactional(readOnly = true)
    public FraudReportResponse getFraudReportById(UUID reportId) {

        FraudReport fraudReport = fraudReportRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fraud report not found with id: " + reportId));

        return fraudReportMapper.toResponse(fraudReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudReportResponse> getAllFraudReports() {
        return fraudReportMapper.toResponseList(fraudReportRepository.findAll());
    }
}
