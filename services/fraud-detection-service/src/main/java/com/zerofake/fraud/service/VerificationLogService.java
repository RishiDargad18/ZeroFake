package com.zerofake.fraud.service;

import com.zerofake.fraud.dto.response.VerificationLogResponse;

import java.util.List;
import java.util.UUID;

public interface VerificationLogService {

    List<VerificationLogResponse> getAllVerificationLogs();

    List<VerificationLogResponse> getVerificationLogsByProductId(UUID productId);

}