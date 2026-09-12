package com.nongthinh.post_service.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePostTopicRequest(
        @NotBlank(message = "POST_CATALOG_NAME_REQUIRED")
        @Size(max = 150, message = "POST_CATALOG_NAME_TOO_LONG")
        String name,
        @NotBlank(message = "POST_TOPIC_SLUG_REQUIRED")
        @Size(max = 180, message = "POST_TOPIC_SLUG_TOO_LONG")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "POST_TOPIC_SLUG_INVALID")
        String slug,
        @Size(max = 500, message = "POST_CATALOG_DESCRIPTION_TOO_LONG")
        String description,
        @Min(value = 0, message = "POST_CATALOG_DISPLAY_ORDER_INVALID")
        @Max(value = 1000000, message = "POST_CATALOG_DISPLAY_ORDER_INVALID")
        int displayOrder
) {
}
