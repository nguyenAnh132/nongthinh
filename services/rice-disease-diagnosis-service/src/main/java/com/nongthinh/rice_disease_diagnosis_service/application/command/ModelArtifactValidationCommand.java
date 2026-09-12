package com.nongthinh.rice_disease_diagnosis_service.application.command;

import java.util.List;
import java.util.UUID;

public record ModelArtifactValidationCommand(
        UUID modelVersionId,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight,
        List<ModelClassManifestItem> classes
) {
}
