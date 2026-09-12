package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.util.List;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

public record DiagnosisImageView(
        DiagnosisFileView file,
        int width,
        int height,
        DiagnosisStatus status,
        List<DiagnosisDetectionView> detections
) {
}
