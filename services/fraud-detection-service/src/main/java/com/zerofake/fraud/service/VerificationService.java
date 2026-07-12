package com.zerofake.fraud.service;

import com.zerofake.fraud.dto.request.VerifyProductRequest;
import com.zerofake.fraud.dto.response.VerificationResponse;

import java.util.UUID;

public interface VerificationService {

    VerificationResponse verifyProduct(VerifyProductRequest request);

    Integer calculateRiskScore(VerifyProductRequest request);
}