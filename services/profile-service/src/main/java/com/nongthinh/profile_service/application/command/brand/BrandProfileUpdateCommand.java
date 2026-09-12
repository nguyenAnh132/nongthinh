package com.nongthinh.profile_service.application.command.brand;

public record BrandProfileUpdateCommand(
    String brandName,
    String taxCode,
    String description,
    String representativeName
) {
}
