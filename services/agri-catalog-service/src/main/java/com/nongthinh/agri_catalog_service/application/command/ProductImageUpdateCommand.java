package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record ProductImageUpdateCommand(
        UUID fileId,
        String altText,
        int displayOrder,
        boolean primary
) {
}
