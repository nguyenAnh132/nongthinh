package com.nongthinh.bo_portal_service.application.view;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import java.time.Instant;

public record SystemParamTypeView(
        Long id,
        String name,
        String description,
        boolean systemDefined,
        Instant createdAt,
        Instant updatedAt
) {
    public static SystemParamTypeView fromSystemParamType(SystemParamType systemParamType) {
        return new SystemParamTypeView(
                systemParamType.getId(),
                systemParamType.getName(),
                systemParamType.getDescription(),
                systemParamType.isSystemDefined(),
                systemParamType.getCreatedAt(),
                systemParamType.getUpdatedAt()
        );
    }
}
