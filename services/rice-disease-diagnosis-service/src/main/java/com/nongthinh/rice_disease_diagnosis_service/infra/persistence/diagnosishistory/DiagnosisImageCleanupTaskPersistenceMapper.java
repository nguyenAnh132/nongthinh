package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisImageCleanupTask;

@Component
public class DiagnosisImageCleanupTaskPersistenceMapper {
    public DiagnosisImageCleanupTask toDomain(JpaDiagnosisImageCleanupTaskEntity entity) {
        return DiagnosisImageCleanupTask.reconstruct(
                entity.getId(),
                entity.getDiagnosisHistoryId(),
                entity.getFileId(),
                entity.getCreatedAt(),
                entity.getAttempts(),
                entity.getLastAttemptAt(),
                entity.getCompletedAt());
    }

    public JpaDiagnosisImageCleanupTaskEntity toEntity(DiagnosisImageCleanupTask task) {
        return JpaDiagnosisImageCleanupTaskEntity.builder()
                .id(task.getId())
                .diagnosisHistoryId(task.getDiagnosisHistoryId())
                .fileId(task.getFileId())
                .createdAt(task.getCreatedAt())
                .attempts(task.getAttempts())
                .lastAttemptAt(task.getLastAttemptAt())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
