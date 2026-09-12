package com.nongthinh.agri_catalog_service.infra.persistence.disease;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDiseaseRepository extends JpaRepository<JpaDiseaseEntity, UUID> {

    boolean existsBySlugIgnoreCaseAndCropTypeIdAndDeletedAtIsNull(
            String slug,
            UUID cropTypeId
    );

    Optional<JpaDiseaseEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<JpaDiseaseEntity> findBySlugIgnoreCaseAndCropTypeIdAndDeletedAtIsNull(
            String slug,
            UUID cropTypeId
    );

    @Query("""
            SELECT d
            FROM JpaDiseaseEntity d
            WHERE d.deletedAt IS NULL
              AND (:brandId IS NULL OR d.brandId = :brandId)
              AND (:reviewStatus IS NULL OR d.reviewStatus = :reviewStatus)
              AND (:cropTypeId IS NULL OR d.cropTypeId = :cropTypeId)
            ORDER BY d.createdAt DESC, d.id DESC
            """)
    List<JpaDiseaseEntity> findAllActive(
            @Param("brandId") UUID brandId,
            @Param("reviewStatus") String reviewStatus,
            @Param("cropTypeId") UUID cropTypeId
    );

    @Query("""
            SELECT d
            FROM JpaDiseaseEntity d
            WHERE d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
              AND (:cropTypeId IS NULL OR d.cropTypeId = :cropTypeId)
            ORDER BY d.publishedAt DESC, d.id DESC
            """)
    List<JpaDiseaseEntity> findAllApproved(@Param("cropTypeId") UUID cropTypeId);

    boolean existsByCropTypeIdAndDeletedAtIsNull(UUID cropTypeId);

    List<JpaDiseaseEntity> findAllByBrandIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID brandId);

    List<JpaDiseaseEntity> findAllByReviewStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            String reviewStatus
    );
}
