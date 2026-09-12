package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record ProductImageCreationCommand(
        UUID fileId,
        String altText,
        int displayOrder,
        boolean primary
) {
}
