package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProductDiseaseTreatmentRepository
        extends JpaRepository<JpaProductDiseaseTreatmentEntity, UUID> {

    boolean existsByProductIdAndDiseaseIdAndDeletedAtIsNull(UUID productId, UUID diseaseId);

    Optional<JpaProductDiseaseTreatmentEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<JpaProductDiseaseTreatmentEntity>
            findAllByProductIdAndDeletedAtIsNullOrderByPriorityAscCreatedAtAsc(UUID productId);

    List<JpaProductDiseaseTreatmentEntity>
            findAllByDiseaseIdAndDeletedAtIsNullOrderByPriorityAscCreatedAtAsc(UUID diseaseId);

    @Query("""
            SELECT t
            FROM JpaProductDiseaseTreatmentEntity t
            JOIN t.product p
            JOIN t.disease d
            WHERE t.deletedAt IS NULL
              AND t.productId = :productId
              AND p.deletedAt IS NULL
              AND p.publicationStatus = 'PUBLISHED'
              AND p.moderationStatus = 'NORMAL'
              AND d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
            ORDER BY t.priority ASC, t.createdAt ASC
            """)
    List<JpaProductDiseaseTreatmentEntity> findAllPublicByProductId(
            @Param("productId") UUID productId
    );

    @Query("""
            SELECT t
            FROM JpaProductDiseaseTreatmentEntity t
            JOIN t.product p
            JOIN t.disease d
            WHERE t.deletedAt IS NULL
              AND t.diseaseId = :diseaseId
              AND p.deletedAt IS NULL
              AND p.publicationStatus = 'PUBLISHED'
              AND p.moderationStatus = 'NORMAL'
              AND d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
            ORDER BY t.priority ASC, t.createdAt ASC
            """)
    List<JpaProductDiseaseTreatmentEntity> findAllPublicByDiseaseId(
            @Param("diseaseId") UUID diseaseId
    );

    @Query("""
            SELECT t
            FROM JpaProductDiseaseTreatmentEntity t
            JOIN FETCH t.product p
            JOIN t.disease d
            WHERE t.deletedAt IS NULL
              AND t.diseaseId = :diseaseId
              AND p.deletedAt IS NULL
              AND p.publicationStatus = 'PUBLISHED'
              AND p.moderationStatus = 'NORMAL'
              AND d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
            ORDER BY t.priority ASC, t.createdAt ASC
            """)
    List<JpaProductDiseaseTreatmentEntity> findPublicRecommendationsByDiseaseId(
            @Param("diseaseId") UUID diseaseId,
            Pageable pageable);
}
