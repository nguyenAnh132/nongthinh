package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import java.util.Locale;
import java.util.UUID;

public record FileMetadata(
        UUID id,
        UUID ownerUserId,
        String purpose,
        String contentType,
        long sizeBytes,
        String publicUrl,
        String visibility,
        String status
) {
    public boolean isUsablePostMedia(UUID expectedOwner) {
        if (expectedOwner == null || !expectedOwner.equals(ownerUserId)
                || !"UPLOADED".equalsIgnoreCase(status)
                || !"PUBLIC".equalsIgnoreCase(visibility)
                || publicUrl == null || publicUrl.isBlank()
                || contentType == null || sizeBytes <= 0) {
            return false;
        }
        return switch (purpose == null ? "" : purpose.toUpperCase(Locale.ROOT)) {
            case "POST_IMAGE" -> contentType.toLowerCase(Locale.ROOT).startsWith("image/");
            case "POST_VIDEO" -> contentType.toLowerCase(Locale.ROOT).startsWith("video/");
            default -> false;
        };
    }

    public MediaType mediaType() {
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("video/")
                ? MediaType.VIDEO
                : MediaType.IMAGE;
    }
}
