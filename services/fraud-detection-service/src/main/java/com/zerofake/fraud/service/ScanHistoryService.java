package com.zerofake.fraud.service;

import com.zerofake.fraud.dto.response.ScanHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface ScanHistoryService {

    List<ScanHistoryResponse> getAllScanHistory();

    List<ScanHistoryResponse> getScanHistoryByProductId(UUID productId);

}