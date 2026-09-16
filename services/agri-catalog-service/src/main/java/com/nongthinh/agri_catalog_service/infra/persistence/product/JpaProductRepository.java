package com.nongthinh.agri_catalog_service.infra.persistence.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface JpaProductRepository extends JpaRepository<JpaProductEntity, UUID> {

    @Query("""
            SELECT p FROM JpaProductEntity p
            WHERE p.deletedAt IS NULL AND p.publicationStatus = 'PUBLISHED' AND p.moderationStatus = 'NORMAL'
              AND LOWER(p.name) LIKE :namePattern ESCAPE '!'
              AND (:diseasePattern = '' OR EXISTS (
                SELECT t.id FROM JpaProductDiseaseTreatmentEntity t JOIN t.disease d
                WHERE t.productId = p.id AND t.deletedAt IS NULL
                  AND d.deletedAt IS NULL AND d.reviewStatus = 'APPROVED'
                  AND LOWER(d.name) LIKE :diseasePattern ESCAPE '!'))
              AND (:keywordPattern = '' OR LOWER(p.name) LIKE :keywordPattern ESCAPE '!' OR EXISTS (
                SELECT kt.id FROM JpaProductDiseaseTreatmentEntity kt JOIN kt.disease kd
                WHERE kt.productId = p.id AND kt.deletedAt IS NULL
                  AND kd.deletedAt IS NULL AND kd.reviewStatus = 'APPROVED'
                  AND LOWER(kd.name) LIKE :keywordPattern ESCAPE '!'))
            ORDER BY p.publishedAt DESC NULLS LAST, p.id DESC
            """)
    Page<JpaProductEntity> searchPublic(@Param("namePattern") String namePattern,
            @Param("diseasePattern") String diseasePattern,
            @Param("keywordPattern") String keywordPattern, Pageable pageable);

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
