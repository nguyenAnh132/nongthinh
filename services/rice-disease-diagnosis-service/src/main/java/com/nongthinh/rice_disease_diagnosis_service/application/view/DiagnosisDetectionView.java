package com.nongthinh.rice_disease_diagnosis_service.application.view;

public record DiagnosisDetectionView(
        String classCode,
        String displayName,
        String classKind,
        float confidence,
        DiagnosisBoundingBoxView boundingBox,
        DiagnosisDiseaseReferenceView disease
) {
}
