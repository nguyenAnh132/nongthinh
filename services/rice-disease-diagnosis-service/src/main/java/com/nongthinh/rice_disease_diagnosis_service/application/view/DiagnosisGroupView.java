package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.util.List;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

public record DiagnosisGroupView(
        DiagnosisStatus status,
        String classCode,
        String displayName,
        DiagnosisDiseaseReferenceView disease,
        List<UUID> fileIds,
        int detectionCount
) {
}
