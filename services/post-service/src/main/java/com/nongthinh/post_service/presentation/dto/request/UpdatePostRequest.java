package com.nongthinh.post_service.presentation.dto.request;

import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdatePostRequest(
        UUID postTypeId,
        UUID topicId,
        @NotBlank(message = "POST_CONTENT_REQUIRED")
        @Size(max = 2_000, message = "POST_CONTENT_TOO_LONG") String content,
        @Size(max = 120, message = "POST_LOCATION_TOO_LONG") String locationText,
        @NotNull(message = "POST_VISIBILITY_REQUIRED") PostVisibility visibility,
        @NotNull(message = "POST_CROP_TYPE_IDS_REQUIRED") Set<UUID> cropTypeIds
) {
}
