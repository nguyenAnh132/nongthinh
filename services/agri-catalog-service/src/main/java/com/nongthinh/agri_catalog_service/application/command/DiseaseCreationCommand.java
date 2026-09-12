package com.nongthinh.agri_catalog_service.application.command;

import java.util.UUID;

public record DiseaseCreationCommand(
        String name,
        String slug,
        String scientificName,
        UUID cropTypeId,
        String affectedPart,
        String pathogenType,
        String shortDescription,
        String description,
        String symptoms,
        String causes,
        String favorableConditions,
        String preventionMethod,
        String treatmentGuideline,
        String thumbnailUrl
) {
}
