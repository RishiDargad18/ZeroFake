package com.zerofake.fraud.repository;

import com.zerofake.fraud.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {

    List<ScanHistory> findByProductId(UUID productId);

    /** Every successful prior scan of a product, which the fraud rules are evaluated against. */
    List<ScanHistory> findByProductIdAndSuccessfulTrue(UUID productId);

    List<ScanHistory> findByUserId(UUID userId);

    Optional<ScanHistory> findTopByProductIdAndSuccessfulTrueOrderByScannedAtDesc(UUID productId);
}
