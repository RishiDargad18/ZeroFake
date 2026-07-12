package com.zerofake.fraud.service;

import com.zerofake.fraud.dto.request.FraudReportRequest;
import com.zerofake.fraud.dto.response.FraudReportResponse;

import java.util.List;
import java.util.UUID;

public interface FraudReportService {

    FraudReportResponse createFraudReport(FraudReportRequest request);

    FraudReportResponse getFraudReportById(UUID reportId);

    List<FraudReportResponse> getAllFraudReports();

}