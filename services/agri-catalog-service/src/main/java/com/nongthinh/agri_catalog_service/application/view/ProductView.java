package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;

public record ProductView(
        UUID id,
        UUID brandId,
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String thumbnailUrl,
        String purchaseUrl,
        PublicationStatus publicationStatus,
        ModerationStatus moderationStatus,
        String moderationReason,
        Instant lockedAt,
        UUID lockedBy,
        Instant publishedAt,
        Instant unpublishedAt,
        boolean featured,
        double averageRating,
        long reviewCount,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static ProductView from(Product product) {
        return from(product, ProductRatingSummary.unrated(product.getId()));
    }

    public static ProductView from(Product product, ProductRatingSummary ratingSummary) {
        return new ProductView(
                product.getId(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getSku(),
                product.getRegistrationNumber(),
                product.getManufacturerName(),
                product.getOriginCountry(),
                product.getShortDescription(),
                product.getDescription(),
                product.getIngredients(),
                product.getUsageInstruction(),
                product.getDosageInstruction(),
                product.getSafetyInstruction(),
                product.getStorageInstruction(),
                product.getWarning(),
                product.getForm(),
                product.getUnit(),
                product.getPackageSpecification(),
                product.getThumbnailUrl(),
                product.getPurchaseUrl(),
                product.getPublicationStatus(),
                product.getModerationStatus(),
                product.getModerationReason(),
                product.getLockedAt(),
                product.getLockedBy(),
                product.getPublishedAt(),
                product.getUnpublishedAt(),
                product.isFeatured(),
                ratingSummary.averageRating(),
                ratingSummary.reviewCount(),
                product.getCreatedAt(),
                product.getCreatedBy(),
                product.getUpdatedAt(),
                product.getUpdatedBy()
        );
    }
}
