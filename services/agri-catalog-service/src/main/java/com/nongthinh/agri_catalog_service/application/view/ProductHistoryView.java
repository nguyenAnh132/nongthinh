package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductHistory;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryActorType;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;

public record ProductHistoryView(
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
    public static ProductHistoryView from(ProductHistory history) {
        return new ProductHistoryView(
                history.getId(),
                history.getProductId(),
                history.getAction(),
                history.getActorId(),
                history.getActorType(),
                history.getPreviousPublicationStatus(),
                history.getNewPublicationStatus(),
                history.getPreviousModerationStatus(),
                history.getNewModerationStatus(),
                history.getReason(),
                history.getChangeSummary(),
                history.getSnapshotData(),
                history.getCreatedAt()
        );
    }
}
