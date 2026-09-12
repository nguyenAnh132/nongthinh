package com.nongthinh.post_service.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePostTopicRequest(
        @NotBlank(message = "POST_CATALOG_NAME_REQUIRED")
        @Size(max = 150, message = "POST_CATALOG_NAME_TOO_LONG")
        String name,
        @Size(max = 500, message = "POST_CATALOG_DESCRIPTION_TOO_LONG")
        String description,
        @Min(value = 0, message = "POST_CATALOG_DISPLAY_ORDER_INVALID")
        @Max(value = 1000000, message = "POST_CATALOG_DISPLAY_ORDER_INVALID")
        int displayOrder,
        @NotNull(message = "POST_CATALOG_ACTIVE_REQUIRED")
        Boolean active
) {
}
