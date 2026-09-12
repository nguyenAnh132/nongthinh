package com.nongthinh.post_service.domain.post;

import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;
import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;

import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTopicId;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Post {
    private final PostId id;
    private final AuthorUserId authorUserId;
    private PostTypeId postTypeId;
    private PostTopicId postTopicId;
    private PostContent content;
    private String locationText;
    private PostVisibility visibility;
    private PostStatus status;
    private List<PostMedia> media;
    private Set<CropTypeId> cropTypeIds;
    private Instant publishedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Post(
            PostId id,
            AuthorUserId authorUserId,
            PostTypeId postTypeId,
            PostTopicId postTopicId,
            PostContent content,
            String locationText,
            PostVisibility visibility,
            PostStatus status,
            List<PostMedia> media,
            Set<CropTypeId> cropTypeIds,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            int maxMedia
    ) {
        this.id = required(id, "postId");
        this.authorUserId = required(authorUserId, "authorUserId");
        this.postTypeId = postTypeId;
        this.postTopicId = postTopicId;
        this.content = required(content, "content");
        this.locationText = optionalText(locationText, 120, "locationText");
        this.visibility = required(visibility, "visibility");
        this.status = required(status, "status");
        this.media = validatedMedia(media, maxMedia);
        this.cropTypeIds = immutableCropTypes(cropTypeIds);
        this.publishedAt = publishedAt;
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        this.deletedAt = deletedAt;
        validateState();
    }

    public static Post createPublished(
            PostId id,
            AuthorUserId authorUserId,
            PostTypeId postTypeId,
            PostTopicId postTopicId,
            PostContent content,
            String locationText,
            PostVisibility visibility,
            List<PostMedia> media,
            Set<CropTypeId> cropTypeIds,
            Instant now,
            int maxMedia
    ) {
        return new Post(id, authorUserId, postTypeId, postTopicId, content, locationText,
                visibility, PostStatus.PUBLISHED, media, cropTypeIds, now, now, now, null,
                maxMedia);
    }

    public static Post createDraft(
            PostId id,
            AuthorUserId authorUserId,
            PostTypeId postTypeId,
            PostTopicId postTopicId,
            PostContent content,
            String locationText,
            PostVisibility visibility,
            List<PostMedia> media,
            Set<CropTypeId> cropTypeIds,
            Instant now,
            int maxMedia
    ) {
        return new Post(id, authorUserId, postTypeId, postTopicId, content, locationText,
                visibility, PostStatus.DRAFT, media, cropTypeIds, null, now, now, null,
                maxMedia);
    }

    public static Post reconstruct(
            PostId id,
            AuthorUserId authorUserId,
            PostTypeId postTypeId,
            PostTopicId postTopicId,
            PostContent content,
            String locationText,
            PostVisibility visibility,
            PostStatus status,
            List<PostMedia> media,
            Set<CropTypeId> cropTypeIds,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            int maxMedia
    ) {
        return new Post(id, authorUserId, postTypeId, postTopicId, content, locationText,
                visibility, status, media, cropTypeIds, publishedAt, createdAt, updatedAt,
                deletedAt, maxMedia);
    }

    public void publish(AuthorUserId actor, Instant now) {
        assertAuthor(actor);
        assertNotDeleted();
        if (status != PostStatus.DRAFT) {
            throw new IllegalStateException("Only a draft post can be published");
        }
        Instant changedAt = changeTime(now);
        status = PostStatus.PUBLISHED;
        publishedAt = changedAt;
        updatedAt = changedAt;
    }

    public void edit(
            AuthorUserId actor,
            PostTypeId postTypeId,
            PostTopicId postTopicId,
            PostContent content,
            String locationText,
            PostVisibility visibility,
            List<PostMedia> media,
            Set<CropTypeId> cropTypeIds,
            int maxMedia,
            Instant now
    ) {
        assertAuthor(actor);
        assertNotDeleted();
        if (status == PostStatus.HIDDEN) {
            throw new IllegalStateException("A hidden post cannot be edited by its author");
        }

        PostContent validatedContent = required(content, "content");
        String validatedLocation = optionalText(locationText, 120, "locationText");
        PostVisibility validatedVisibility = required(visibility, "visibility");
        List<PostMedia> validatedMedia = validatedMedia(media, maxMedia);
        Set<CropTypeId> validatedCropTypes = immutableCropTypes(cropTypeIds);
        Instant changedAt = changeTime(now);

        this.postTypeId = postTypeId;
        this.postTopicId = postTopicId;
        this.content = validatedContent;
        this.locationText = validatedLocation;
        this.visibility = validatedVisibility;
        this.media = validatedMedia;
        this.cropTypeIds = validatedCropTypes;
        this.updatedAt = changedAt;
    }

    public void changeVisibility(AuthorUserId actor, PostVisibility visibility, Instant now) {
        assertAuthor(actor);
        assertNotDeleted();
        if (status == PostStatus.HIDDEN) {
            throw new IllegalStateException("A hidden post cannot change visibility");
        }
        PostVisibility validatedVisibility = required(visibility, "visibility");
        Instant changedAt = changeTime(now);
        this.visibility = validatedVisibility;
        this.updatedAt = changedAt;
    }

    public void addMedia(AuthorUserId actor, PostMedia item, int maxMedia, Instant now) {
        assertAuthor(actor);
        assertEditable();
        List<PostMedia> changed = new ArrayList<>(media);
        changed.add(required(item, "media item"));
        this.media = validatedMedia(changed, maxMedia);
        this.updatedAt = changeTime(now);
    }

    public void replaceMedia(AuthorUserId actor, UUID mediaId, PostMedia replacement,
                             int maxMedia, Instant now) {
        assertAuthor(actor);
        assertEditable();
        required(mediaId, "mediaId");
        required(replacement, "replacement");
        List<PostMedia> changed = new ArrayList<>(media);
        int index = -1;
        for (int i = 0; i < changed.size(); i++) {
            if (changed.get(i).getId().equals(mediaId)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("post media does not belong to the post");
        }
        if (!replacement.getId().equals(mediaId)) {
            throw new IllegalArgumentException("replacement media id must not change");
        }
        changed.set(index, replacement);
        this.media = validatedMedia(changed, maxMedia);
        this.updatedAt = changeTime(now);
    }

    public void removeMedia(AuthorUserId actor, UUID mediaId, int maxMedia, Instant now) {
        assertAuthor(actor);
        assertEditable();
        required(mediaId, "mediaId");
        List<PostMedia> changed = media.stream()
                .filter(item -> !item.getId().equals(mediaId))
                .toList();
        if (changed.size() == media.size()) {
            throw new IllegalArgumentException("post media does not belong to the post");
        }
        this.media = validatedMedia(changed, maxMedia);
        this.updatedAt = changeTime(now);
    }

    public void hide(Instant now) {
        assertNotDeleted();
        if (status != PostStatus.PUBLISHED) {
            throw new IllegalStateException("Only a published post can be hidden");
        }
        Instant changedAt = changeTime(now);
        status = PostStatus.HIDDEN;
        updatedAt = changedAt;
    }

    public void restore(Instant now) {
        assertNotDeleted();
        if (status != PostStatus.HIDDEN) {
            throw new IllegalStateException("Only a hidden post can be restored");
        }
        Instant changedAt = changeTime(now);
        status = PostStatus.PUBLISHED;
        updatedAt = changedAt;
    }

    public void softDelete(AuthorUserId actor, Instant now) {
        assertAuthor(actor);
        softDeleteByModerator(now);
    }

    public void softDeleteByModerator(Instant now) {
        assertNotDeleted();
        Instant changedAt = changeTime(now);
        deletedAt = changedAt;
        updatedAt = changedAt;
    }

    public boolean isVisibleInPublicFeed() {
        return status == PostStatus.PUBLISHED
                && visibility == PostVisibility.PUBLIC
                && deletedAt == null;
    }

    private void assertAuthor(AuthorUserId actor) {
        if (!authorUserId.equals(required(actor, "actor"))) {
            throw new IllegalArgumentException("Only the post author may perform this action");
        }
    }

    private void assertNotDeleted() {
        if (deletedAt != null) {
            throw new IllegalStateException("A deleted post cannot be changed");
        }
    }

    private void assertEditable() {
        assertNotDeleted();
        if (status == PostStatus.HIDDEN) {
            throw new IllegalStateException("A hidden post cannot be edited by its author");
        }
    }

    private void validateState() {
        if (status == PostStatus.DRAFT && publishedAt != null) {
            throw new IllegalArgumentException("A draft post must not have publishedAt");
        }
        if (status != PostStatus.DRAFT && publishedAt == null) {
            throw new IllegalArgumentException("A published or hidden post must have publishedAt");
        }
        notBefore(updatedAt, createdAt, "updatedAt", "createdAt");
        if (publishedAt != null) notBefore(updatedAt, publishedAt, "updatedAt", "publishedAt");
        if (deletedAt != null) notBefore(updatedAt, deletedAt, "updatedAt", "deletedAt");
    }

    private Instant changeTime(Instant now) {
        return notBefore(now, updatedAt, "now", "updatedAt");
    }

    private static List<PostMedia> validatedMedia(List<PostMedia> values, int maxMedia) {
        required(values, "media");
        if (maxMedia < 0 || values.size() > maxMedia) {
            throw new IllegalArgumentException("media count exceeds the configured limit");
        }
        Set<FileId> fileIds = new HashSet<>();
        Set<Integer> displayOrders = new HashSet<>();
        for (PostMedia item : values) {
            required(item, "media item");
            if (!fileIds.add(item.getFileId())) {
                throw new IllegalArgumentException("media fileId must be unique within a post");
            }
            if (!displayOrders.add(item.getDisplayOrder())) {
                throw new IllegalArgumentException("media displayOrder must be unique within a post");
            }
        }
        return List.copyOf(values);
    }

    private static Set<CropTypeId> immutableCropTypes(Set<CropTypeId> values) {
        required(values, "cropTypeIds");
        if (values.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("cropTypeIds must not contain null");
        }
        return Set.copyOf(values);
    }

    public PostId getId() { return id; }
    public AuthorUserId getAuthorUserId() { return authorUserId; }
    public PostTypeId getPostTypeId() { return postTypeId; }
    public PostTopicId getPostTopicId() { return postTopicId; }
    public PostContent getContent() { return content; }
    public String getLocationText() { return locationText; }
    public PostVisibility getVisibility() { return visibility; }
    public PostStatus getStatus() { return status; }
    public List<PostMedia> getMedia() { return media; }
    public Set<CropTypeId> getCropTypeIds() { return cropTypeIds; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public boolean isDeleted() { return deletedAt != null; }
}
