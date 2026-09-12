package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;

public record ProductImageView(
        UUID id,
        UUID productId,
        UUID fileId,
        String imageUrl,
        String altText,
        int displayOrder,
        boolean primary,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static ProductImageView from(ProductImage productImage) {
        return new ProductImageView(
                productImage.getId(),
                productImage.getProductId(),
                productImage.getFileId(),
                productImage.getImageUrl(),
                productImage.getAltText(),
                productImage.getDisplayOrder(),
                productImage.isPrimary(),
                productImage.getCreatedAt(),
                productImage.getCreatedBy(),
                productImage.getUpdatedAt(),
                productImage.getUpdatedBy()
        );
    }
}
