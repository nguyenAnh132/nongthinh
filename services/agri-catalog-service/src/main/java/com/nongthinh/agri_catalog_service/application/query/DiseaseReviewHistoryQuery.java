package com.nongthinh.agri_catalog_service.application.query;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public record DiseaseReviewHistoryQuery(
        CreatedSource createdSource,
        UUID diseaseId,
        UUID brandId,
        DiseaseReviewAction action,
        ReviewStatus newStatus,
        ReviewActorType actorType,
        UUID actorId,
        String keyword,
        Instant from,
        Instant to,
        int page,
        int size
) {
}
