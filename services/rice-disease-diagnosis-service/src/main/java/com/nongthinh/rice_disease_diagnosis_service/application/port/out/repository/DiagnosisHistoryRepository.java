package com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;

public interface DiagnosisHistoryRepository {
    DiagnosisHistory appendAndTrim(DiagnosisHistory history);

    List<DiagnosisHistory> findLatestByFarmerUserId(UUID farmerUserId);

    Optional<DiagnosisHistory> findByIdAndFarmerUserId(UUID id, UUID farmerUserId);
}
