package com.nongthinh.agri_catalog_service.infra.persistence.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductRepository extends JpaRepository<JpaProductEntity, UUID> {

    boolean existsByBrandIdAndSlugIgnoreCaseAndDeletedAtIsNull(UUID brandId, String slug);

    Optional<JpaProductEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<JpaProductEntity> findByBrandIdAndSlugIgnoreCaseAndDeletedAtIsNull(
            UUID brandId,
            String slug
    );

    List<JpaProductEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    List<JpaProductEntity> findAllByBrandIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID brandId);

    List<JpaProductEntity> findAllByCategoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID categoryId);

    List<JpaProductEntity> findAllByPublicationStatusAndModerationStatusAndDeletedAtIsNullOrderByPublishedAtDesc(
            String publicationStatus,
            String moderationStatus
    );
}
