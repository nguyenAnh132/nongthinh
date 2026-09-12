package com.nongthinh.post_service.domain.history;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;

import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record PostSnapshot(
        String content,
        UUID postTypeId,
        UUID postTopicId,
        Set<UUID> cropTypeIds,
        String locationText,
        List<MediaSnapshot> media,
        PostStatus status,
        PostVisibility visibility
) {
    public PostSnapshot {
        content = requiredText(content, 2_000, "content");
        if (postTypeId != null) requiredId(postTypeId, "postTypeId");
        if (postTopicId != null) requiredId(postTopicId, "postTopicId");
        locationText = optionalText(locationText, 120, "locationText");
        cropTypeIds = immutableIds(cropTypeIds, "cropTypeIds");
        media = immutableMedia(media);
        status = required(status, "status");
        visibility = required(visibility, "visibility");
    }

    public static PostSnapshot from(Post post) {
        return new PostSnapshot(
                post.getContent().value(),
                post.getPostTypeId() == null ? null : post.getPostTypeId().value(),
                post.getPostTopicId() == null ? null : post.getPostTopicId().value(),
                post.getCropTypeIds().stream().map(id -> id.value()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                post.getLocationText(),
                post.getMedia().stream().map(MediaSnapshot::from).toList(),
                post.getStatus(),
                post.getVisibility()
        );
    }

    public record MediaSnapshot(
            UUID fileId,
            String mediaUrl,
            String contentType,
            int displayOrder
    ) {
        public MediaSnapshot {
            requiredId(fileId, "fileId");
            mediaUrl = requiredText(mediaUrl, 1_000, "mediaUrl");
            contentType = requiredText(contentType, 100, "contentType");
            if (displayOrder < 0) {
                throw new IllegalArgumentException("displayOrder must not be negative");
            }
        }

        private static MediaSnapshot from(PostMedia media) {
            return new MediaSnapshot(media.getFileId().value(), media.getMediaUrl(),
                    media.getContentType(), media.getDisplayOrder());
        }
    }

    private static Set<UUID> immutableIds(Set<UUID> values, String field) {
        required(values, field);
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(field + " must not contain null");
        }
        return Set.copyOf(values);
    }

    private static List<MediaSnapshot> immutableMedia(List<MediaSnapshot> values) {
        required(values, "media");
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("media must not contain null");
        }
        return List.copyOf(values);
    }
}
