package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import java.time.Instant;
import java.util.UUID;

public record PostMediaView(
        UUID id,
        UUID fileId,
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
    public static PostMediaView from(PostMedia media) {
        return new PostMediaView(
                media.getId(), media.getFileId().value(), media.getMediaType(),
                media.getMediaUrl(), media.getContentType(), media.getWidth(),
                media.getHeight(), media.getSizeBytes(), media.getDisplayOrder(),
                media.getCaption(), media.getCreatedAt()
        );
    }
}
