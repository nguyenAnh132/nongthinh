package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

public record DiseaseView(
        UUID id,
        CreatedSource createdSource,
        UUID brandId,
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
        String thumbnailUrl,
        ReviewStatus reviewStatus,
        String rejectionReason,
        Instant submittedAt,
        Instant reviewedAt,
        UUID reviewedBy,
        Instant publishedAt,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static DiseaseView from(Disease disease) {
        return new DiseaseView(
                disease.getId(),
                disease.getCreatedSource(),
                disease.getBrandId(),
                disease.getName(),
                disease.getSlug(),
                disease.getScientificName(),
                disease.getCropTypeId(),
                disease.getAffectedPart(),
                disease.getPathogenType(),
                disease.getShortDescription(),
                disease.getDescription(),
                disease.getSymptoms(),
                disease.getCauses(),
                disease.getFavorableConditions(),
                disease.getPreventionMethod(),
                disease.getTreatmentGuideline(),
                disease.getThumbnailUrl(),
                disease.getReviewStatus(),
                disease.getRejectionReason(),
                disease.getSubmittedAt(),
                disease.getReviewedAt(),
                disease.getReviewedBy(),
                disease.getPublishedAt(),
                disease.getCreatedAt(),
                disease.getCreatedBy(),
                disease.getUpdatedAt(),
                disease.getUpdatedBy()
        );
    }
}
