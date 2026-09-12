package com.nongthinh.agri_catalog_service.application.view;

import java.util.List;
import java.util.UUID;

public record AiModelDeploymentResolutionView(
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
        List<AiModelVersionClassView> classes
) {
}
