package com.zerofake.fraud.service;

import com.zerofake.fraud.dto.request.VerifyProductRequest;
import com.zerofake.fraud.dto.response.VerificationResponse;

public interface VerificationService {

    VerificationResponse verifyProduct(VerifyProductRequest request);
}
