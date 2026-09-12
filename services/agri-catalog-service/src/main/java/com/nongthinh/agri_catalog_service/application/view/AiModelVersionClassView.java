package com.nongthinh.agri_catalog_service.application.view;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;

public record AiModelVersionClassView(
        UUID id,
        int classIndex,
        String classCode,
        String displayName,
        AiModelClassKind classKind
) {
    public static AiModelVersionClassView from(AiModelVersionClass item) {
        return new AiModelVersionClassView(
                item.getId(), item.getClassIndex(), item.getClassCode(), item.getDisplayName(), item.getClassKind());
    }
}
