package com.nongthinh.agri_catalog_service.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiseaseUpdateRequest(
        @NotBlank(message = "DISEASE_NAME_REQUIRED")
        @Size(max = 255, message = "DISEASE_NAME_TOO_LONG")
        String name,

        @NotBlank(message = "DISEASE_SLUG_REQUIRED")
        @Size(max = 280, message = "DISEASE_SLUG_TOO_LONG")
        String slug,

        @Size(max = 255, message = "DISEASE_SCIENTIFIC_NAME_TOO_LONG")
        String scientificName,

        @NotNull(message = "DISEASE_CROP_TYPE_ID_REQUIRED")
        UUID cropTypeId,

        @Size(max = 100, message = "DISEASE_AFFECTED_PART_TOO_LONG")
        String affectedPart,

        @Size(max = 50, message = "DISEASE_PATHOGEN_TYPE_TOO_LONG")
        String pathogenType,

        @Size(max = 500, message = "DISEASE_SHORT_DESCRIPTION_TOO_LONG")
        String shortDescription,

        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,

        @Size(max = 2000, message = "DISEASE_THUMBNAIL_URL_TOO_LONG")
        String thumbnailUrl
) {
}
