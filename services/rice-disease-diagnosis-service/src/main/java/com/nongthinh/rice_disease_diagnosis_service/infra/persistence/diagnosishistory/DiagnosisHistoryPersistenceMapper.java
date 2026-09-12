package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

@Component
public class DiagnosisHistoryPersistenceMapper {
    public DiagnosisHistory toDomain(JpaDiagnosisHistoryEntity entity) {
        return DiagnosisHistory.reconstruct(
                entity.getId(), entity.getFarmerUserId(), entity.getCropTypeId(), entity.getModelId(),
                entity.getModelVersionId(), entity.getModelVersion(), DiagnosisStatus.valueOf(entity.getStatus()),
                entity.getResultSnapshot(), entity.getFileIds(), entity.getCreatedAt());
    }

    public JpaDiagnosisHistoryEntity toEntity(DiagnosisHistory history) {
        return JpaDiagnosisHistoryEntity.builder()
                .id(history.getId())
                .farmerUserId(history.getFarmerUserId())
                .cropTypeId(history.getCropTypeId())
                .modelId(history.getModelId())
                .modelVersionId(history.getModelVersionId())
                .modelVersion(history.getModelVersion())
                .status(history.getStatus().name())
                .resultSnapshot(history.getResultSnapshot())
                .fileIds(history.getFileIds())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
