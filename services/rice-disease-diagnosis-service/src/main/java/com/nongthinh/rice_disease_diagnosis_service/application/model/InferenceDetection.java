package com.nongthinh.rice_disease_diagnosis_service.application.model;

public record InferenceDetection(
        int classIndex,
        String classCode,
        String displayName,
        String classKind,
        float confidence,
        BoundingBox boundingBox
) {
    public boolean isDisease() { return "DISEASE".equals(classKind); }
    public boolean isHealthy() { return "HEALTHY".equals(classKind); }
}
