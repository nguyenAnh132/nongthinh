package com.nongthinh.post_service.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdatePostMediaRequest(
        @NotNull(message = "POST_MEDIA_FILE_ID_REQUIRED") UUID fileId,
        @PositiveOrZero(message = "POST_MEDIA_DISPLAY_ORDER_INVALID") int displayOrder,
        @Size(max = 500, message = "POST_MEDIA_CAPTION_TOO_LONG") String caption
) {
}
