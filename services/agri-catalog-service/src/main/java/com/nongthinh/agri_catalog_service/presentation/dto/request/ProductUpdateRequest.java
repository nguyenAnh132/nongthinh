package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotNull(message = "PRODUCT_CATEGORY_ID_REQUIRED")
        UUID categoryId,

        @NotBlank(message = "PRODUCT_NAME_REQUIRED")
        @Size(max = 255, message = "PRODUCT_NAME_TOO_LONG")
        String name,

        @NotBlank(message = "PRODUCT_SLUG_REQUIRED")
        @Size(max = 280, message = "PRODUCT_SLUG_TOO_LONG")
        String slug,

        @Size(max = 100, message = "PRODUCT_SKU_TOO_LONG")
        String sku,

        @Size(max = 100, message = "PRODUCT_REGISTRATION_NUMBER_TOO_LONG")
        String registrationNumber,

        @Size(max = 255, message = "PRODUCT_MANUFACTURER_NAME_TOO_LONG")
        String manufacturerName,

        @Size(max = 100, message = "PRODUCT_ORIGIN_COUNTRY_TOO_LONG")
        String originCountry,

        @Size(max = 500, message = "PRODUCT_SHORT_DESCRIPTION_TOO_LONG")
        String shortDescription,

        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,

        @Size(max = 100, message = "PRODUCT_FORM_TOO_LONG")
        String form,

        @Size(max = 50, message = "PRODUCT_UNIT_TOO_LONG")
        String unit,

        @Size(max = 255, message = "PRODUCT_PACKAGE_SPECIFICATION_TOO_LONG")
        String packageSpecification,

        String purchaseUrl
) {
}
