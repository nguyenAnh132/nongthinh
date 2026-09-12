package com.nongthinh.location_service.application.command;

public record ProvinceCreationCommand(
    String id,
    String code,
    String name
) {
}
