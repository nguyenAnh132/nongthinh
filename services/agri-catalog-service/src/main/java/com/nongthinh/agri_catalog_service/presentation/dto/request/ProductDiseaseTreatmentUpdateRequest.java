package com.nongthinh.agri_catalog_service.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductDiseaseTreatmentUpdateRequest(
        @Pattern(
                regexp = "LOW|MEDIUM|HIGH|VERY_HIGH",
                message = "PRODUCT_DISEASE_TREATMENT_EFFECTIVENESS_LEVEL_INVALID"
        )
        String effectivenessLevel,

        @PositiveOrZero(message = "PRODUCT_DISEASE_TREATMENT_PRIORITY_INVALID")
        int priority,

        @Size(max = 255, message = "PRODUCT_DISEASE_TREATMENT_DOSAGE_TOO_LONG")
        String dosage,

        String applicationMethod,
        String applicationTiming,

        @Size(max = 255, message = "PRODUCT_DISEASE_TREATMENT_FREQUENCY_TOO_LONG")
        String frequencyInstruction,

        String treatmentNote
) {
}
