package com.zerofake.fraud.repository;

import com.zerofake.fraud.constant.FraudStatus;
import com.zerofake.fraud.constant.FraudType;
import com.zerofake.fraud.entity.FraudReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FraudReportRepository extends JpaRepository<FraudReport, UUID> {

    @Override
    Optional<FraudReport> findById(UUID id);

    List<FraudReport> findByProductId(UUID productId);

    List<FraudReport> findByFraudType(FraudType fraudType);

    List<FraudReport> findByStatus(FraudStatus status);

}