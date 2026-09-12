package com.nongthinh.bo_portal_service.application.view;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import java.time.Instant;

public record SystemParamView(
        Long id,
        String name,
        String value,
        String description,
        String dataType,
        boolean systemDefined,
        Long typeId,
        String typeName,
        Instant createdAt,
        Instant updatedAt
) {
    public static SystemParamView fromSystemParam(SystemParam systemParam, String typeName) {
        return new SystemParamView(
                systemParam.getId(),
                systemParam.getName(),
                systemParam.getValue(),
                systemParam.getDescription(),
                systemParam.getDataType().toString(),
                systemParam.isSystemDefined(),
                systemParam.getTypeId(),
                typeName,
                systemParam.getCreatedAt(),
                systemParam.getUpdatedAt()
        );
    }
}
