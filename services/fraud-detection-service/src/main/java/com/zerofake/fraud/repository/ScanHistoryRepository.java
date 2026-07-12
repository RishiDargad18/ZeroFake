package com.zerofake.fraud.repository;

import com.zerofake.fraud.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {

    @Override
    Optional<ScanHistory> findById(UUID id);

    List<ScanHistory> findByProductId(UUID productId);

    List<ScanHistory> findByUserId(UUID userId);

    List<ScanHistory> findBySuccessful(Boolean successful);

    Optional<ScanHistory> findTopByProductIdAndSuccessfulTrueOrderByScannedAtDesc(UUID productId);

}