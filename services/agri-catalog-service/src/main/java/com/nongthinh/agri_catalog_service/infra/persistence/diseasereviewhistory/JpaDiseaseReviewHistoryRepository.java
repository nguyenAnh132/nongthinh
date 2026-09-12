package com.nongthinh.agri_catalog_service.infra.persistence.diseasereviewhistory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDiseaseReviewHistoryRepository
        extends JpaRepository<JpaDiseaseReviewHistoryEntity, UUID> {

    List<JpaDiseaseReviewHistoryEntity> findAllByDiseaseIdOrderByCreatedAtDesc(UUID diseaseId);
}
