package com.nongthinh.rice_disease_diagnosis_service.application.model;

import java.util.UUID;

public record ModelClassManifest(
        UUID id,
        int classIndex,
        String classCode,
        String displayName,
        String classKind
) {
    public boolean isDisease() { return "DISEASE".equals(classKind); }
    public boolean isHealthy() { return "HEALTHY".equals(classKind); }
}
