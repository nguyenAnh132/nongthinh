package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record PostView(
        UUID id,
        UUID authorUserId,
        UUID postTypeId,
        UUID topicId,
        String content,
        String locationText,
        PostVisibility visibility,
        PostStatus status,
        List<PostMediaView> media,
        Set<UUID> cropTypeIds,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public static PostView from(Post post) {
        List<PostMediaView> media = post.getMedia().stream()
                .sorted(Comparator.comparingInt(item -> item.getDisplayOrder()))
                .map(PostMediaView::from)
                .toList();
        Set<UUID> cropTypeIds = post.getCropTypeIds().stream()
                .map(item -> item.value())
                .collect(Collectors.toUnmodifiableSet());
        return new PostView(
                post.getId().value(), post.getAuthorUserId().value(),
                post.getPostTypeId() == null ? null : post.getPostTypeId().value(),
                post.getPostTopicId() == null ? null : post.getPostTopicId().value(),
                post.getContent().value(), post.getLocationText(), post.getVisibility(),
                post.getStatus(), media, cropTypeIds, post.getPublishedAt(),
                post.getCreatedAt(), post.getUpdatedAt(), post.getDeletedAt()
        );
    }
}
