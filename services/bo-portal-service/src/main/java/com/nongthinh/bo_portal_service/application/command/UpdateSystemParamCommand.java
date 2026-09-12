package com.nongthinh.bo_portal_service.application.command;

public record UpdateSystemParamCommand(
        String value,
        String description
) {
}
