package com.nongthinh.rice_disease_diagnosis_service.application.command;

import java.util.UUID;

public record ModelRuntimeWarmupCommand(
        UUID modelVersionId,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight
) {
}
