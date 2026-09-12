package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.util.List;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

public record DiagnosisResponseView(
        UUID diagnosisId,
        UUID cropTypeId,
        DiagnosisModelView model,
        DiagnosisStatus status,
        List<DiagnosisImageView> images,
        List<DiagnosisGroupView> groups,
        long processingTimeMs
) {
}
