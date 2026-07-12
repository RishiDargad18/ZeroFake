package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.dto.response.ScanHistoryResponse;
import com.zerofake.fraud.mapper.ScanHistoryMapper;
import com.zerofake.fraud.repository.ScanHistoryRepository;
import com.zerofake.fraud.service.ScanHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScanHistoryServiceImpl implements ScanHistoryService {

    private final ScanHistoryRepository scanHistoryRepository;
    private final ScanHistoryMapper scanHistoryMapper;

    @Override
    public List<ScanHistoryResponse> getAllScanHistory() {
        return scanHistoryMapper.toResponseList(scanHistoryRepository.findAll());
    }

    @Override
    public List<ScanHistoryResponse> getScanHistoryByProductId(UUID productId) {
        return scanHistoryMapper.toResponseList(
                scanHistoryRepository.findByProductId(productId)
        );
    }

}