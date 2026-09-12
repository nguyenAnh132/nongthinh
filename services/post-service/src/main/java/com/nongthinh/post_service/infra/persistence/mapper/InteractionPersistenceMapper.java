package com.nongthinh.post_service.infra.persistence.mapper;

import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.comment.valueobject.CommentStatus;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import com.nongthinh.post_service.domain.interaction.PostShare;
import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkId;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCommentEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostReactionEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostReportEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostShareEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface InteractionPersistenceMapper {
    default Comment toDomain(JpaPostCommentEntity value) {
        return Comment.reconstruct(value.getId(), value.getPostId(), value.getParentCommentId(),
                value.getAuthorUserId(), value.getContent(), CommentStatus.valueOf(value.getStatus()),
                value.getCreatedAt(), value.getUpdatedAt(), value.getDeletedAt());
    }

    JpaPostCommentEntity toEntity(Comment value);

    default Reaction toDomain(JpaPostReactionEntity value) {
        return Reaction.reconstruct(value.getId(), value.getPostId(), value.getActorId(),
                ReactionType.valueOf(value.getReactionType()), value.getCreatedAt(), value.getUpdatedAt());
    }

    @Mapping(target = "reactionType", source = "type")
    JpaPostReactionEntity toEntity(Reaction value);

    @Mapping(target = "postId", source = "id.postId")
    @Mapping(target = "userId", source = "id.userId")
    Bookmark toDomain(JpaPostBookmarkEntity value);

    @Mapping(target = "id", expression = "java(toBookmarkId(value))")
    JpaPostBookmarkEntity toEntity(Bookmark value);

    default JpaPostBookmarkId toBookmarkId(Bookmark value) {
        return new JpaPostBookmarkId(value.postId(), value.userId());
    }

    PostShare toDomain(JpaPostShareEntity value);

    JpaPostShareEntity toEntity(PostShare value);

    default PostReport toDomain(JpaPostReportEntity value) {
        return PostReport.reconstruct(value.getId(), value.getPostId(), value.getReporterId(),
                ReportReason.valueOf(value.getReasonCode()), value.getReasonDetail(),
                ReportStatus.valueOf(value.getStatus()), value.getResolvedBy(), value.getResolvedAt(),
                value.getResolutionNote(), value.getCreatedAt(), value.getUpdatedAt());
    }

    @Mapping(target = "reasonCode", source = "reason")
    JpaPostReportEntity toEntity(PostReport value);
}
