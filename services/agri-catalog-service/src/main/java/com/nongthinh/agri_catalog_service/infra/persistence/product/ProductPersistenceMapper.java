package com.nongthinh.agri_catalog_service.infra.persistence.product;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;

@Mapper(componentModel = "spring")
public interface ProductPersistenceMapper {

    default Product toDomain(JpaProductEntity entity) {
        return Product.reconstruct(
                entity.getId(),
                entity.getBrandId(),
                entity.getCategoryId(),
                entity.getName(),
                entity.getSlug(),
                entity.getSku(),
                entity.getRegistrationNumber(),
                entity.getManufacturerName(),
                entity.getOriginCountry(),
                entity.getShortDescription(),
                entity.getDescription(),
                entity.getIngredients(),
                entity.getUsageInstruction(),
                entity.getDosageInstruction(),
                entity.getSafetyInstruction(),
                entity.getStorageInstruction(),
                entity.getWarning(),
                entity.getForm(),
                entity.getUnit(),
                entity.getPackageSpecification(),
                entity.getThumbnailUrl(),
                entity.getPurchaseUrl(),
                PublicationStatus.fromString(entity.getPublicationStatus()),
                ModerationStatus.fromString(entity.getModerationStatus()),
                entity.getModerationReason(),
                entity.getLockedAt(),
                entity.getLockedBy(),
                entity.getPublishedAt(),
                entity.getUnpublishedAt(),
                entity.isFeatured(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaProductEntity toEntity(Product product) {
        return JpaProductEntity.builder()
                .id(product.getId())
                .brandId(product.getBrandId())
                .categoryId(product.getCategoryId())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .registrationNumber(product.getRegistrationNumber())
                .manufacturerName(product.getManufacturerName())
                .originCountry(product.getOriginCountry())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .ingredients(product.getIngredients())
                .usageInstruction(product.getUsageInstruction())
                .dosageInstruction(product.getDosageInstruction())
                .safetyInstruction(product.getSafetyInstruction())
                .storageInstruction(product.getStorageInstruction())
                .warning(product.getWarning())
                .form(product.getForm())
                .unit(product.getUnit())
                .packageSpecification(product.getPackageSpecification())
                .thumbnailUrl(product.getThumbnailUrl())
                .purchaseUrl(product.getPurchaseUrl())
                .publicationStatus(product.getPublicationStatus().name())
                .moderationStatus(product.getModerationStatus().name())
                .moderationReason(product.getModerationReason())
                .lockedAt(product.getLockedAt())
                .lockedBy(product.getLockedBy())
                .publishedAt(product.getPublishedAt())
                .unpublishedAt(product.getUnpublishedAt())
                .isFeatured(product.isFeatured())
                .createdAt(product.getCreatedAt())
                .createdBy(product.getCreatedBy())
                .updatedAt(product.getUpdatedAt())
                .updatedBy(product.getUpdatedBy())
                .deletedAt(product.getDeletedAt())
                .deletedBy(product.getDeletedBy())
                .build();
    }
}
