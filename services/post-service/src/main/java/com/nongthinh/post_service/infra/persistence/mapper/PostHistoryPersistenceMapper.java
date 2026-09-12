package com.nongthinh.post_service.infra.persistence.mapper;

import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PostHistoryPersistenceMapper {
    @Mapping(target = "snapshotData", source = "snapshot")
    JpaPostHistoryEntity toEntity(PostHistory domain);

    default PostHistory toDomain(JpaPostHistoryEntity entity) {
        return PostHistory.reconstruct(entity.getId(), entity.getPostId(), entity.getPostAuthorUserId(),
                entity.getActorUserId(), HistoryActorType.valueOf(entity.getActorType()),
                PostHistoryAction.valueOf(entity.getAction()), status(entity.getPreviousStatus()),
                status(entity.getNewStatus()), visibility(entity.getPreviousVisibility()),
                visibility(entity.getNewVisibility()), entity.getReasonCode(), entity.getReasonDetail(),
                entity.getReportId(), entity.getSnapshotData(), entity.getCreatedAt());
    }

    private static PostStatus status(String value) { return value == null ? null : PostStatus.valueOf(value); }
    private static PostVisibility visibility(String value) { return value == null ? null : PostVisibility.valueOf(value); }
}
