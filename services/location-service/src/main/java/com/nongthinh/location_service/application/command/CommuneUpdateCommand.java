package com.nongthinh.location_service.application.command;

public record CommuneUpdateCommand(
    String provinceId,
    String code,
    String name
) {
}
