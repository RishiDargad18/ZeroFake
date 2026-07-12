package com.zerofake.fraud.service.impl;

import com.zerofake.fraud.dto.response.VerificationLogResponse;
import com.zerofake.fraud.mapper.VerificationLogMapper;
import com.zerofake.fraud.repository.VerificationLogRepository;
import com.zerofake.fraud.service.VerificationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationLogServiceImpl implements VerificationLogService {

    private final VerificationLogRepository verificationLogRepository;
    private final VerificationLogMapper verificationLogMapper;

    @Override
    public List<VerificationLogResponse> getAllVerificationLogs() {
        return verificationLogMapper.toResponseList(verificationLogRepository.findAll());
    }

    @Override
    public List<VerificationLogResponse> getVerificationLogsByProductId(UUID productId) {
        return verificationLogMapper.toResponseList(
                verificationLogRepository.findByProductId(productId)
        );
    }

}