package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryActorType;

public class ProductHistory {

    private final UUID id;
    private final UUID productId;

    private final ProductHistoryAction action;
    private final UUID actorId;
    private final ProductHistoryActorType actorType;

    private final PublicationStatus previousPublicationStatus;
    private final PublicationStatus newPublicationStatus;

    private final ModerationStatus previousModerationStatus;
    private final ModerationStatus newModerationStatus;

    private final String reason;
    private final String changeSummary;

    private final String snapshotData;

    private final Instant createdAt;

    private ProductHistory(
        UUID id,
        UUID productId,
        ProductHistoryAction action,
        UUID actorId,
        ProductHistoryActorType actorType,
        PublicationStatus previousPublicationStatus,
        PublicationStatus newPublicationStatus,
        ModerationStatus previousModerationStatus,
        ModerationStatus newModerationStatus,
        String reason,
        String changeSummary,
        String snapshotData,
        Instant createdAt
    ) {
        this.id = id;
        this.productId = productId;
        this.action = action;
        this.actorId = actorId;
        this.actorType = actorType;
        this.previousPublicationStatus = previousPublicationStatus;
        this.newPublicationStatus = newPublicationStatus;
        this.previousModerationStatus = previousModerationStatus;
        this.newModerationStatus = newModerationStatus;
        this.reason = reason;
        this.changeSummary = changeSummary;
        this.snapshotData = snapshotData;
        this.createdAt = createdAt;
    }

    public static ProductHistory create(
        UUID id,
        UUID productId,
        ProductHistoryAction action,
        UUID actorId,
        ProductHistoryActorType actorType,
        PublicationStatus previousPublicationStatus,
        PublicationStatus newPublicationStatus,
        ModerationStatus previousModerationStatus,
        ModerationStatus newModerationStatus,
        String reason,
        String changeSummary,
        String snapshotData,
        Instant now
    ) {
        validateRequiredFields(
            id,
            productId,
            action,
            actorId,
            actorType,
            now
        );

        return new ProductHistory(
            id,
            productId,
            action,
            actorId,
            actorType,
            previousPublicationStatus,
            newPublicationStatus,
            previousModerationStatus,
            newModerationStatus,
            reason,
            changeSummary,
            snapshotData,
            now
        );
    }

    public static ProductHistory reconstruct(
        UUID id,
        UUID productId,
        ProductHistoryAction action,
        UUID actorId,
        ProductHistoryActorType actorType,
        PublicationStatus previousPublicationStatus,
        PublicationStatus newPublicationStatus,
        ModerationStatus previousModerationStatus,
        ModerationStatus newModerationStatus,
        String reason,
        String changeSummary,
        String snapshotData,
        Instant createdAt
    ) {
        return new ProductHistory(
            id,
            productId,
            action,
            actorId,
            actorType,
            previousPublicationStatus,
            newPublicationStatus,
            previousModerationStatus,
            newModerationStatus,
            reason,
            changeSummary,
            snapshotData,
            createdAt
        );
    }

    private static void validateRequiredFields(
        UUID id,
        UUID productId,
        ProductHistoryAction action,
        UUID actorId,
        ProductHistoryActorType actorType,
        Instant createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                "Product history ID must not be null"
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException(
                "Product ID must not be null"
            );
        }

        if (action == null) {
            throw new IllegalArgumentException(
                "Product history action must not be null"
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

    public UUID getProductId() {
        return productId;
    }

    public ProductHistoryAction getAction() {
        return action;
    }

    public UUID getActorId() {
        return actorId;
    }

    public ProductHistoryActorType getActorType() {
        return actorType;
    }

    public PublicationStatus getPreviousPublicationStatus() {
        return previousPublicationStatus;
    }

    public PublicationStatus getNewPublicationStatus() {
        return newPublicationStatus;
    }

    public ModerationStatus getPreviousModerationStatus() {
        return previousModerationStatus;
    }

    public ModerationStatus getNewModerationStatus() {
        return newModerationStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}