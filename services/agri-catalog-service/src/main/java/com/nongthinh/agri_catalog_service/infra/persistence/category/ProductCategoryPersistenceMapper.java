package com.nongthinh.agri_catalog_service.infra.persistence.category;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;

@Mapper(componentModel = "spring")
public interface ProductCategoryPersistenceMapper {

    default ProductCategory toDomain(JpaProductCategoryEntity entity) {
        return ProductCategory.reconstruct(
                entity.getId(),
                entity.getParentId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaProductCategoryEntity toEntity(ProductCategory productCategory) {
        return JpaProductCategoryEntity.builder()
                .id(productCategory.getId())
                .parentId(productCategory.getParentId())
                .name(productCategory.getName())
                .slug(productCategory.getSlug())
                .description(productCategory.getDescription())
                .displayOrder(productCategory.getDisplayOrder())
                .isActive(productCategory.isActive())
                .createdAt(productCategory.getCreatedAt())
                .createdBy(productCategory.getCreatedBy())
                .updatedAt(productCategory.getUpdatedAt())
                .updatedBy(productCategory.getUpdatedBy())
                .deletedAt(productCategory.getDeletedAt())
                .deletedBy(productCategory.getDeletedBy())
                .build();
    }
}
