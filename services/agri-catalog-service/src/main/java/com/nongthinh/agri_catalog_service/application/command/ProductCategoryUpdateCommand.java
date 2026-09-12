package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record ProductCategoryUpdateCommand(
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean active
) {
}
