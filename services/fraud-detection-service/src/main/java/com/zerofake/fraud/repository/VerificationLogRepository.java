package com.zerofake.fraud.repository;

import com.zerofake.fraud.constant.VerificationResult;
import com.zerofake.fraud.entity.VerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationLogRepository extends JpaRepository<VerificationLog, UUID> {

    @Override
    Optional<VerificationLog> findById(UUID id);

    List<VerificationLog> findByProductId(UUID productId);

    List<VerificationLog> findByFraudDetected(Boolean fraudDetected);

    List<VerificationLog> findByVerificationResult(VerificationResult verificationResult);

}