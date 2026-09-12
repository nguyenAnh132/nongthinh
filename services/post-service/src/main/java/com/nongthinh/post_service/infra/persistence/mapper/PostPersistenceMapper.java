package com.nongthinh.post_service.infra.persistence.mapper;

import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTopicId;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCropTypeEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCropTypeId;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostMediaEntity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = JpaPostCropTypeId.class
)
public interface PostPersistenceMapper {
    default Post toDomain(JpaPostEntity entity, List<JpaPostMediaEntity> media,
                          List<JpaPostCropTypeEntity> cropTypes, PostLimitsProperties limits) {
        List<PostMedia> domainMedia = media.stream().map(this::toDomain).toList();
        Set<CropTypeId> domainCropTypes = cropTypes.stream()
                .map(item -> new CropTypeId(item.getId().getCropTypeId()))
                .collect(Collectors.toUnmodifiableSet());
        return Post.reconstruct(
                new PostId(entity.getId()),
                new AuthorUserId(entity.getAuthorUserId()),
                entity.getPostTypeId() == null ? null : new PostTypeId(entity.getPostTypeId()),
                entity.getTopicId() == null ? null : new PostTopicId(entity.getTopicId()),
                new PostContent(entity.getContent()),
                entity.getLocationText(),
                PostVisibility.valueOf(entity.getVisibility()),
                PostStatus.valueOf(entity.getStatus()),
                domainMedia,
                domainCropTypes,
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                limits.mediaPerPost()
        );
    }

    default PostMedia toDomain(JpaPostMediaEntity entity) {
        return PostMedia.reconstruct(
                entity.getId(), new FileId(entity.getFileId()),
                MediaType.valueOf(entity.getMediaType()), entity.getMediaUrl(),
                entity.getContentType(), entity.getWidth(), entity.getHeight(),
                entity.getSizeBytes(), entity.getDisplayOrder(), entity.getCaption(),
                entity.getCreatedAt()
        );
    }

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "authorUserId", source = "authorUserId.value")
    @Mapping(target = "postTypeId", source = "postTypeId.value")
    @Mapping(target = "topicId", source = "postTopicId.value")
    @Mapping(target = "content", source = "content.value")
    @Mapping(target = "reactionVersion", ignore = true)
    @Mapping(target = "commentVersion", ignore = true)
    JpaPostEntity toEntity(Post post);

    @Mapping(target = "postId", source = "postId")
    @Mapping(target = "fileId", source = "media.fileId.value")
    @Mapping(target = "mediaType", source = "media.mediaType")
    JpaPostMediaEntity toMediaEntity(PostMedia media, java.util.UUID postId);

    default List<JpaPostMediaEntity> toMediaEntities(Post post) {
        java.util.UUID postId = post.getId().value();
        return post.getMedia().stream()
                .map(media -> toMediaEntity(media, postId))
                .toList();
    }

    @Mapping(
            target = "id",
            expression = "java(new JpaPostCropTypeId(postId, cropTypeId.value()))"
    )
    JpaPostCropTypeEntity toCropTypeEntity(
            CropTypeId cropTypeId,
            java.util.UUID postId,
            java.time.Instant createdAt
    );

    default List<JpaPostCropTypeEntity> toCropTypeEntities(Post post) {
        java.util.UUID postId = post.getId().value();
        return post.getCropTypeIds().stream()
                .map(cropTypeId -> toCropTypeEntity(cropTypeId, postId, post.getUpdatedAt()))
                .toList();
    }
}
