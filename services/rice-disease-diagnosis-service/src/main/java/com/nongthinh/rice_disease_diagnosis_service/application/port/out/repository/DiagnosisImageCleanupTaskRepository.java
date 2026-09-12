package com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisImageCleanupTask;

public interface DiagnosisImageCleanupTaskRepository {
    void enqueue(UUID diagnosisHistoryId, UUID fileId, Instant createdAt);

    List<DiagnosisImageCleanupTask> findPending(int limit);

    boolean isReferencedByExistingHistory(UUID fileId);

    DiagnosisImageCleanupTask save(DiagnosisImageCleanupTask task);
}
