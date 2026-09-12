package com.nongthinh.bo_portal_service.application.command;

public record CreateSystemParamCommand(
        String name,
        String value,
        String description,
        String dataType,
        Long typeId
) {
}
