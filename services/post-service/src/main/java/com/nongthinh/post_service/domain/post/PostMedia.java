package com.nongthinh.post_service.domain.post;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredText;

import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import java.time.Instant;
import java.util.UUID;

public final class PostMedia {
    private final UUID id;
    private final FileId fileId;
    private final MediaType mediaType;
    private final String mediaUrl;
    private final String contentType;
    private final Integer width;
    private final Integer height;
    private final Long sizeBytes;
    private final int displayOrder;
    private final String caption;
    private final Instant createdAt;

    private PostMedia(
            UUID id,
            FileId fileId,
            MediaType mediaType,
            String mediaUrl,
            String contentType,
            Integer width,
            Integer height,
            Long sizeBytes,
            int displayOrder,
            String caption,
            Instant createdAt
    ) {
        this.id = requiredId(id, "postMediaId");
        this.fileId = required(fileId, "fileId");
        this.mediaType = required(mediaType, "mediaType");
        this.mediaUrl = requiredText(mediaUrl, 1_000, "mediaUrl");
        this.contentType = requiredText(contentType, 100, "contentType");
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.displayOrder = displayOrder;
        this.caption = optionalText(caption, 500, "caption");
        this.createdAt = required(createdAt, "createdAt");
        validateSnapshot();
    }

    public static PostMedia create(
            UUID id,
            FileId fileId,
            MediaType mediaType,
            String mediaUrl,
            String contentType,
            Integer width,
            Integer height,
            Long sizeBytes,
            int displayOrder,
            String caption,
            Instant now
    ) {
        return new PostMedia(id, fileId, mediaType, mediaUrl, contentType, width, height,
                sizeBytes, displayOrder, caption, now);
    }

    public static PostMedia reconstruct(
            UUID id,
            FileId fileId,
            MediaType mediaType,
            String mediaUrl,
            String contentType,
            Integer width,
            Integer height,
            Long sizeBytes,
            int displayOrder,
            String caption,
            Instant createdAt
    ) {
        return new PostMedia(id, fileId, mediaType, mediaUrl, contentType, width, height,
                sizeBytes, displayOrder, caption, createdAt);
    }

    public PostMedia updateSnapshot(
            FileId fileId,
            MediaType mediaType,
            String mediaUrl,
            String contentType,
            Integer width,
            Integer height,
            Long sizeBytes,
            int displayOrder,
            String caption
    ) {
        return new PostMedia(id, fileId, mediaType, mediaUrl, contentType, width, height,
                sizeBytes, displayOrder, caption, createdAt);
    }

    private void validateSnapshot() {
        if (!mediaType.accepts(contentType)) {
            throw new IllegalArgumentException("contentType does not match mediaType");
        }
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder must not be negative");
        }
        if ((width == null) != (height == null)) {
            throw new IllegalArgumentException("width and height must both be present or absent");
        }
        if (width != null && (width <= 0 || height <= 0)) {
            throw new IllegalArgumentException("media dimensions must be positive");
        }
        if (sizeBytes != null && sizeBytes <= 0) {
            throw new IllegalArgumentException("sizeBytes must be positive");
        }
    }

    public UUID getId() { return id; }
    public FileId getFileId() { return fileId; }
    public MediaType getMediaType() { return mediaType; }
    public String getMediaUrl() { return mediaUrl; }
    public String getContentType() { return contentType; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public Long getSizeBytes() { return sizeBytes; }
    public int getDisplayOrder() { return displayOrder; }
    public String getCaption() { return caption; }
    public Instant getCreatedAt() { return createdAt; }
}
