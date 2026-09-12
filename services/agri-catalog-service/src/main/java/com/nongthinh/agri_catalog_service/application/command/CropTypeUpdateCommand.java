package com.nongthinh.agri_catalog_service.application.command;

public record CropTypeUpdateCommand(
        String name,
        String description,
        boolean active
) {
}

