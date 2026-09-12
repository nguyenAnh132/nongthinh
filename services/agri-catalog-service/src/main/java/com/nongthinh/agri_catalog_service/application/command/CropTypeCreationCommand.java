package com.nongthinh.agri_catalog_service.application.command;

public record CropTypeCreationCommand(
        String code,
        String name,
        String description
) {
}

