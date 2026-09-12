package com.nongthinh.bo_portal_service.application.view;

import java.time.Instant;
import java.util.List;

public record SystemParamTypeGroupView(
        Long id,
        String name,
        String description,
        boolean systemDefined,
        Instant createdAt,
        Instant updatedAt,
        List<SystemParamView> params
) {
}
