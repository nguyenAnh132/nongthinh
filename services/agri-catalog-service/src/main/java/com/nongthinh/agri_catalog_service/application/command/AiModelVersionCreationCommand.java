package com.nongthinh.agri_catalog_service.application.command;

import java.util.List;
import java.util.UUID;

public record AiModelVersionCreationCommand(
        String version,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight,
        List<AiModelVersionClassCommand> classes
) {
}
