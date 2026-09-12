package com.nongthinh.agri_catalog_service.infra.persistence.producthistory;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.ProductHistory;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryActorType;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductHistoryPersistenceMapper {

    private final ObjectMapper objectMapper;

    public ProductHistory toDomain(JpaProductHistoryEntity entity) {
        return ProductHistory.reconstruct(
                entity.getId(),
                entity.getProductId(),
                ProductHistoryAction.fromString(entity.getAction()),
                entity.getActorId(),
                ProductHistoryActorType.fromString(entity.getActorType()),
                publicationStatus(entity.getPreviousPublicationStatus()),
                publicationStatus(entity.getNewPublicationStatus()),
                moderationStatus(entity.getPreviousModerationStatus()),
                moderationStatus(entity.getNewModerationStatus()),
                entity.getReason(),
                entity.getChangeSummary(),
                entity.getSnapshotData() == null ? null : entity.getSnapshotData().toString(),
                entity.getCreatedAt()
        );
    }

    public JpaProductHistoryEntity toEntity(ProductHistory history) {
        return JpaProductHistoryEntity.builder()
                .id(history.getId())
                .productId(history.getProductId())
                .action(history.getAction().getValue())
                .actorId(history.getActorId())
                .actorType(history.getActorType().getValue())
                .previousPublicationStatus(value(history.getPreviousPublicationStatus()))
                .newPublicationStatus(value(history.getNewPublicationStatus()))
                .previousModerationStatus(value(history.getPreviousModerationStatus()))
                .newModerationStatus(value(history.getNewModerationStatus()))
                .reason(history.getReason())
                .changeSummary(history.getChangeSummary())
                .snapshotData(parseSnapshot(history.getSnapshotData()))
                .createdAt(history.getCreatedAt())
                .build();
    }

    private PublicationStatus publicationStatus(String value) {
        return value == null ? null : PublicationStatus.fromString(value);
    }

    private ModerationStatus moderationStatus(String value) {
        return value == null ? null : ModerationStatus.fromString(value);
    }

    private String value(PublicationStatus status) {
        return status == null ? null : status.name();
    }

    private String value(ModerationStatus status) {
        return status == null ? null : status.name();
    }

    private com.fasterxml.jackson.databind.JsonNode parseSnapshot(String snapshot) {
        if (snapshot == null) {
            return null;
        }
        try {
            return objectMapper.readTree(snapshot);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.EVENT_SERIALIZATION_FAILED, ex);
        }
    }
}
