package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductCategoryUpdateRequest(

        UUID parentId,

        @NotBlank(message = "PRODUCT_CATEGORY_NAME_REQUIRED")
        @Size(max = 150, message = "PRODUCT_CATEGORY_NAME_TOO_LONG")
        String name,

        @NotBlank(message = "PRODUCT_CATEGORY_SLUG_REQUIRED")
        @Size(max = 180, message = "PRODUCT_CATEGORY_SLUG_TOO_LONG")
        String slug,

        String description,

        @PositiveOrZero(message = "PRODUCT_CATEGORY_DISPLAY_ORDER_INVALID")
        int displayOrder,

        @NotNull(message = "PRODUCT_CATEGORY_ACTIVE_REQUIRED")
        Boolean active
) {
}
