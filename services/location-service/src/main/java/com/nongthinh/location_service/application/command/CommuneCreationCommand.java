package com.nongthinh.location_service.application.command;

public record CommuneCreationCommand(
    String provinceId,
    String code,
    String name
) {
}
