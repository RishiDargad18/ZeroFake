package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.dto.request.FraudReportRequest;
import com.zerofake.fraud.dto.response.FraudReportResponse;
import com.zerofake.fraud.entity.FraudReport;
import com.zerofake.fraud.exception.ResourceNotFoundException;
import com.zerofake.fraud.mapper.FraudReportMapper;
import com.zerofake.fraud.repository.FraudReportRepository;
import com.zerofake.fraud.service.FraudReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudReportServiceImpl implements FraudReportService {

    private final FraudReportRepository fraudReportRepository;
    private final FraudReportMapper fraudReportMapper;

    @Override
    public FraudReportResponse createFraudReport(FraudReportRequest request) {

        FraudReport fraudReport = FraudReport.builder()
                .productId(request.getProductId())
                .description(request.getDescription())
                .status(FraudStatus.OPEN)
                .detectedAt(LocalDateTime.now())
                .build();

        FraudReport savedReport = fraudReportRepository.save(fraudReport);

        return fraudReportMapper.toResponse(savedReport);
    }

    @Override
    public FraudReportResponse getFraudReportById(java.util.UUID reportId) {

        FraudReport fraudReport = fraudReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud report not found with id: " + reportId));

        return fraudReportMapper.toResponse(fraudReport);
    }

    @Override
    public List<FraudReportResponse> getAllFraudReports() {
        return fraudReportMapper.toResponseList(fraudReportRepository.findAll());
    }

}