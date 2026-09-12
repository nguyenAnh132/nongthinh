package com.nongthinh.agri_catalog_service.application.command;

import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;

public record AiModelVersionClassCommand(
        int classIndex,
        String classCode,
        String displayName,
        AiModelClassKind classKind
) {
}
