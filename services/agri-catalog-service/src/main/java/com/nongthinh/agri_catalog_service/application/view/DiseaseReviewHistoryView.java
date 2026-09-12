package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.DiseaseReviewHistory;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public record DiseaseReviewHistoryView(
        UUID id,
        UUID diseaseId,
        DiseaseReviewAction action,
        ReviewStatus previousStatus,
        ReviewStatus newStatus,
        String comment,
        UUID actorId,
        ReviewActorType actorType,
        Instant createdAt
) {
    public static DiseaseReviewHistoryView from(DiseaseReviewHistory history) {
        return new DiseaseReviewHistoryView(
                history.getId(),
                history.getDiseaseId(),
                history.getAction(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getComment(),
                history.getActorId(),
                history.getActorType(),
                history.getCreatedAt()
        );
    }
}
