package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;

public record ProductCategoryView(
        UUID id,
        UUID parentId,
        String name,
        String slug,
        String description,
        int displayOrder,
        boolean active,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static ProductCategoryView from(ProductCategory productCategory) {
        return new ProductCategoryView(
                productCategory.getId(),
                productCategory.getParentId(),
                productCategory.getName(),
                productCategory.getSlug(),
                productCategory.getDescription(),
                productCategory.getDisplayOrder(),
                productCategory.isActive(),
                productCategory.getCreatedAt(),
                productCategory.getCreatedBy(),
                productCategory.getUpdatedAt(),
                productCategory.getUpdatedBy()
        );
    }
}
