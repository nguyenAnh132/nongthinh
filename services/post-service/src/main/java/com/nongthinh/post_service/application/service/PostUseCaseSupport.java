package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.model.FileMetadata;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.CropTypeQuery;
import com.nongthinh.post_service.application.port.out.FileQuery;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostFeaturesProperties;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTopicId;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostUseCaseSupport {
    private final PostRepository postRepository;
    private final PostTypeRepository postTypeRepository;
    private final PostTopicRepository postTopicRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FileQuery fileQuery;
    private final CropTypeQuery cropTypeQuery;
    private final PostLimitsProperties limits;
    private final PostFeaturesProperties features;

    public Post requirePost(UUID id) {
        return postRepository.findById(new PostId(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Post requirePostForUpdate(UUID id) {
        return postRepository.findByIdForUpdate(new PostId(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Post requirePostIncludingDeleted(UUID id) {
        return postRepository.findByIdIncludingDeleted(new PostId(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Post requirePostForUpdateIncludingDeleted(UUID id) {
        return postRepository.findByIdForUpdateIncludingDeleted(new PostId(id))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Post requireReadablePost(UUID id) {
        Post post = requirePost(id);
        if (post.isVisibleInPublicFeed()) {
            return post;
        }
        UUID viewerId = currentUserProvider.findCurrentUserId().orElse(null);
        if (viewerId != null && viewerId.equals(post.getAuthorUserId().value())) {
            return post;
        }
        throw new BusinessException(ErrorCode.POST_NOT_FOUND);
    }

    public Post requireInteractablePost(UUID id) {
        Post post = requirePost(id);
        if (!post.isVisibleInPublicFeed()) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    public AuthorUserId currentAuthor() {
        return new AuthorUserId(currentUserProvider.getCurrentUserId());
    }

    public void assertOwner(Post post, AuthorUserId actor) {
        if (!post.getAuthorUserId().equals(actor)) {
            throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
        }
    }

    public void assertEditable(Post post) {
        if (post.getStatus() == PostStatus.HIDDEN) {
            throw new BusinessException(ErrorCode.POST_NOT_EDITABLE);
        }
    }

    public PostTypeId activePostTypeOrNull(UUID id) {
        if (id == null) {
            return null;
        }
        return postTypeRepository.findById(id)
                .filter(item -> item.isActive())
                .map(item -> new PostTypeId(item.getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_TYPE_NOT_FOUND));
    }

    public PostTopicId requireActiveTopic(UUID id) {
        if (id == null) {
            return null;
        }
        return postTopicRepository.findById(id)
                .filter(item -> item.isActive())
                .map(item -> new PostTopicId(item.getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_TOPIC_NOT_FOUND));
    }

    public PostContent content(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.POST_CONTENT_REQUIRED);
        }
        if (value.trim().length() > limits.contentLength()) {
            throw new BusinessException(ErrorCode.POST_CONTENT_TOO_LONG);
        }
        return new PostContent(value);
    }

    public PostVisibility visibility(PostVisibility value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.POST_VISIBILITY_REQUIRED);
        }
        if (!features.allowedVisibility().contains(value)) {
            throw new BusinessException(ErrorCode.POST_VISIBILITY_NOT_ALLOWED);
        }
        return value;
    }

    public Set<CropTypeId> cropTypeIds(Set<UUID> values) {
        return cropTypeIds(values, Set.of());
    }

    public Set<CropTypeId> cropTypeIds(Set<UUID> values, Set<CropTypeId> existingValues) {
        if (values == null || values.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.POST_CROP_TYPE_IDS_REQUIRED);
        }
        if (values.isEmpty()) {
            return Set.of();
        }
        Set<UUID> requestedIds = Set.copyOf(values);
        Set<UUID> existingIds = existingValues.stream()
                .map(CropTypeId::value)
                .collect(Collectors.toUnmodifiableSet());
        Set<UUID> addedIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toUnmodifiableSet());
        if (!addedIds.isEmpty() && !cropTypeQuery.findActiveIds(addedIds).containsAll(addedIds)) {
            throw new BusinessException(ErrorCode.POST_CROP_TYPE_NOT_FOUND);
        }
        return requestedIds.stream()
                .map(CropTypeId::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > limits.pageSize()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }

    public String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String normalized = keyword.trim();
        if (normalized.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        return normalized;
    }

    public FileMetadata requireValidPostMedia(UUID fileId, UUID ownerUserId) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.POST_MEDIA_FILE_ID_REQUIRED);
        }
        FileMetadata file = fileQuery.getById(fileId);
        if (!file.isUsablePostMedia(ownerUserId)
                || !features.allowedMediaType().contains(file.mediaType())) {
            throw new BusinessException(ErrorCode.POST_MEDIA_FILE_INVALID);
        }
        return file;
    }

    public PostMedia requireMedia(Post post, UUID mediaId) {
        if (mediaId == null) {
            throw new BusinessException(ErrorCode.POST_MEDIA_NOT_FOUND);
        }
        return post.getMedia().stream()
                .filter(item -> item.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_MEDIA_NOT_FOUND));
    }

    public void assertMediaSlotAvailable(Post post, UUID mediaIdToIgnore, UUID fileId,
                                         int displayOrder) {
        if (displayOrder < 0) {
            throw new BusinessException(ErrorCode.POST_MEDIA_DISPLAY_ORDER_INVALID);
        }
        boolean duplicate = post.getMedia().stream()
                .filter(item -> !item.getId().equals(mediaIdToIgnore))
                .anyMatch(item -> item.getFileId().value().equals(fileId)
                        || item.getDisplayOrder() == displayOrder);
        if (duplicate) {
            throw new BusinessException(ErrorCode.POST_MEDIA_CONFLICT);
        }
        if (mediaIdToIgnore == null && post.getMedia().size() >= limits.mediaPerPost()) {
            throw new BusinessException(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);
        }
    }

    public int maxMedia() {
        return limits.mediaPerPost();
    }
}
