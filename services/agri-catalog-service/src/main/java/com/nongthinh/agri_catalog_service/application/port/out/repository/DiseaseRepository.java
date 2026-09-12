package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public interface DiseaseRepository {

    boolean existsBySlugAndCropTypeId(String slug, UUID cropTypeId);

    Optional<Disease> findById(UUID id);

    Optional<Disease> findBySlugAndCropTypeId(String slug, UUID cropTypeId);

    List<Disease> findAll(UUID brandId, ReviewStatus reviewStatus, UUID cropTypeId);

    List<Disease> findAllApproved(UUID cropTypeId);

    boolean existsByCropTypeId(UUID cropTypeId);

    List<Disease> findAllByBrandId(UUID brandId);

    List<Disease> findAllByReviewStatus(ReviewStatus reviewStatus);

    Disease save(Disease disease);
}
