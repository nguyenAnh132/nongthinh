package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public record DiseaseReviewHistoryListItemView(
        UUID id,
        UUID diseaseId,
        String diseaseName,
        UUID cropTypeId,
        CreatedSource createdSource,
        UUID brandId,
        DiseaseReviewAction action,
        ReviewStatus previousStatus,
        ReviewStatus newStatus,
        String comment,
        UUID actorId,
        ReviewActorType actorType,
        Instant createdAt
) {
}
