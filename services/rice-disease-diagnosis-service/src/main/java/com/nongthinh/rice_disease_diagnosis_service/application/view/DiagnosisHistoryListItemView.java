package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

public record DiagnosisHistoryListItemView(
        UUID id,
        UUID cropTypeId,
        UUID modelId,
        UUID modelVersionId,
        String modelVersion,
        DiagnosisStatus status,
        Instant createdAt
) {
    public static DiagnosisHistoryListItemView from(DiagnosisHistory history) {
        return new DiagnosisHistoryListItemView(
                history.getId(), history.getCropTypeId(), history.getModelId(), history.getModelVersionId(),
                history.getModelVersion(), history.getStatus(), history.getCreatedAt());
    }
}
