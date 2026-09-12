package com.nongthinh.agri_catalog_service.infra.persistence.product_image;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;

@Mapper(componentModel = "spring")
public interface ProductImagePersistenceMapper {

    default ProductImage toDomain(JpaProductImageEntity entity) {
        return ProductImage.reconstruct(
                entity.getId(),
                entity.getProductId(),
                entity.getFileId(),
                entity.getImageUrl(),
                entity.getAltText(),
                entity.getDisplayOrder(),
                entity.isPrimary(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaProductImageEntity toEntity(ProductImage productImage) {
        return JpaProductImageEntity.builder()
                .id(productImage.getId())
                .productId(productImage.getProductId())
                .fileId(productImage.getFileId())
                .imageUrl(productImage.getImageUrl())
                .altText(productImage.getAltText())
                .displayOrder(productImage.getDisplayOrder())
                .isPrimary(productImage.isPrimary())
                .createdAt(productImage.getCreatedAt())
                .createdBy(productImage.getCreatedBy())
                .updatedAt(productImage.getUpdatedAt())
                .updatedBy(productImage.getUpdatedBy())
                .deletedAt(productImage.getDeletedAt())
                .deletedBy(productImage.getDeletedBy())
                .build();
    }
}
