package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductImageCreationRequest(
        @NotNull(message = "PRODUCT_IMAGE_FILE_ID_REQUIRED")
        UUID fileId,

        @Size(max = 255, message = "PRODUCT_IMAGE_ALT_TEXT_TOO_LONG")
        String altText,

        @PositiveOrZero(message = "PRODUCT_IMAGE_DISPLAY_ORDER_INVALID")
        int displayOrder,

        @NotNull(message = "PRODUCT_IMAGE_PRIMARY_REQUIRED")
        Boolean primary
) {
}
