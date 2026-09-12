package com.nongthinh.rice_disease_diagnosis_service.application.model;

import java.util.List;
import java.util.UUID;

public record ActiveModelDeployment(
        UUID deploymentId,
        UUID modelId,
        String modelCode,
        String modelName,
        UUID modelVersionId,
        String modelVersion,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight,
        List<ModelClassManifest> classes
) {
}
