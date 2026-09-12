package com.nongthinh.agri_catalog_service.infra.persistence.diseasereviewhistory;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.disease.DiseaseReviewHistory;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

@Mapper(componentModel = "spring")
public interface DiseaseReviewHistoryPersistenceMapper {

    default DiseaseReviewHistory toDomain(JpaDiseaseReviewHistoryEntity entity) {
        return DiseaseReviewHistory.reconstruct(
                entity.getId(),
                entity.getDiseaseId(),
                DiseaseReviewAction.fromString(entity.getAction()),
                entity.getPreviousStatus() == null
                        ? null
                        : ReviewStatus.fromString(entity.getPreviousStatus()),
                ReviewStatus.fromString(entity.getNewStatus()),
                entity.getComment(),
                entity.getActorId(),
                ReviewActorType.fromString(entity.getActorType()),
                entity.getCreatedAt()
        );
    }

    default JpaDiseaseReviewHistoryEntity toEntity(DiseaseReviewHistory history) {
        return JpaDiseaseReviewHistoryEntity.builder()
                .id(history.getId())
                .diseaseId(history.getDiseaseId())
                .action(history.getAction().getValue())
                .previousStatus(
                        history.getPreviousStatus() == null
                                ? null
                                : history.getPreviousStatus().getValue()
                )
                .newStatus(history.getNewStatus().getValue())
                .comment(history.getComment())
                .actorId(history.getActorId())
                .actorType(history.getActorType().getValue())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
