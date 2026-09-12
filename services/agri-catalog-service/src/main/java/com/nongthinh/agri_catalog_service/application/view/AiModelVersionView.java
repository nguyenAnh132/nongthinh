package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;

public record AiModelVersionView(
        UUID id,
        UUID modelId,
        String version,
        UUID artifactFileId,
        String artifactSha256,
        int inputWidth,
        int inputHeight,
        AiModelVersionStatus status,
        String validationReport,
        List<AiModelVersionClassView> classes,
        Instant createdAt,
        UUID createdBy,
        Instant validatedAt,
        UUID validatedBy,
        Instant retiredAt,
        UUID retiredBy
) {
    public static AiModelVersionView from(AiModelVersion version) {
        return new AiModelVersionView(
                version.getId(),
                version.getModelId(),
                version.getVersion(),
                version.getArtifactFileId(),
                version.getArtifactSha256(),
                version.getInputWidth(),
                version.getInputHeight(),
                version.getStatus(),
                version.getValidationReport(),
                version.getClasses().stream().map(AiModelVersionClassView::from).toList(),
                version.getCreatedAt(),
                version.getCreatedBy(),
                version.getValidatedAt(),
                version.getValidatedBy(),
                version.getRetiredAt(),
                version.getRetiredBy()
        );
    }
}
