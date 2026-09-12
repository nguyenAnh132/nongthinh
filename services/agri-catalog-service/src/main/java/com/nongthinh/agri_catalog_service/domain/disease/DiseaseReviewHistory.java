package com.nongthinh.agri_catalog_service.domain.disease;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;

public class DiseaseReviewHistory {

    private final UUID id;
    private final UUID diseaseId;

    private final DiseaseReviewAction action;
    private final ReviewStatus previousStatus;
    private final ReviewStatus newStatus;

    private final String comment;

    private final UUID actorId;
    private final ReviewActorType actorType;

    private final Instant createdAt;

    private DiseaseReviewHistory(
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
        this.id = id;
        this.diseaseId = diseaseId;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comment = comment;
        this.actorId = actorId;
        this.actorType = actorType;
        this.createdAt = createdAt;
    }

    public static DiseaseReviewHistory create(
        UUID id,
        UUID diseaseId,
        DiseaseReviewAction action,
        ReviewStatus previousStatus,
        ReviewStatus newStatus,
        String comment,
        UUID actorId,
        ReviewActorType actorType,
        Instant now
    ) {
        validateRequiredFields(
            id,
            diseaseId,
            action,
            newStatus,
            actorId,
            actorType,
            now
        );

        return new DiseaseReviewHistory(
            id,
            diseaseId,
            action,
            previousStatus,
            newStatus,
            comment,
            actorId,
            actorType,
            now
        );
    }

    public static DiseaseReviewHistory reconstruct(
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
        return new DiseaseReviewHistory(
            id,
            diseaseId,
            action,
            previousStatus,
            newStatus,
            comment,
            actorId,
            actorType,
            createdAt
        );
    }

    private static void validateRequiredFields(
        UUID id,
        UUID diseaseId,
        DiseaseReviewAction action,
        ReviewStatus newStatus,
        UUID actorId,
        ReviewActorType actorType,
        Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                "Disease review history ID must not be null"
            );
        }

        if (diseaseId == null) {
            throw new IllegalArgumentException(
                "Disease ID must not be null"
            );
        }

        if (action == null) {
            throw new IllegalArgumentException(
                "Disease review action must not be null"
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                "New review status must not be null"
            );
        }

        if (actorId == null) {
            throw new IllegalArgumentException(
                "Actor ID must not be null"
            );
        }

        if (actorType == null) {
            throw new IllegalArgumentException(
                "Actor type must not be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                "Created time must not be null"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getDiseaseId() {
        return diseaseId;
    }

    public DiseaseReviewAction getAction() {
        return action;
    }

    public ReviewStatus getPreviousStatus() {
        return previousStatus;
    }

    public ReviewStatus getNewStatus() {
        return newStatus;
    }

    public String getComment() {
        return comment;
    }

    public UUID getActorId() {
        return actorId;
    }

    public ReviewActorType getActorType() {
        return actorType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}