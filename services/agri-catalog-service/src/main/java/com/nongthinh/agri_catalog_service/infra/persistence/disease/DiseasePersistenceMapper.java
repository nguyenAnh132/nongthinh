package com.nongthinh.agri_catalog_service.infra.persistence.disease;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;

@Mapper(componentModel = "spring")
public interface DiseasePersistenceMapper {

    default Disease toDomain(JpaDiseaseEntity entity) {
        return Disease.reconstruct(
                entity.getId(),
                CreatedSource.fromString(entity.getCreatedSource()),
                entity.getBrandId(),
                entity.getName(),
                entity.getSlug(),
                entity.getScientificName(),
                entity.getCropTypeId(),
                entity.getAffectedPart(),
                entity.getPathogenType(),
                entity.getShortDescription(),
                entity.getDescription(),
                entity.getSymptoms(),
                entity.getCauses(),
                entity.getFavorableConditions(),
                entity.getPreventionMethod(),
                entity.getTreatmentGuideline(),
                entity.getThumbnailUrl(),
                ReviewStatus.fromString(entity.getReviewStatus()),
                entity.getRejectionReason(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewedBy(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaDiseaseEntity toEntity(Disease disease) {
        return JpaDiseaseEntity.builder()
                .id(disease.getId())
                .createdSource(disease.getCreatedSource().getValue())
                .brandId(disease.getBrandId())
                .name(disease.getName())
                .slug(disease.getSlug())
                .scientificName(disease.getScientificName())
                .cropTypeId(disease.getCropTypeId())
                .affectedPart(disease.getAffectedPart())
                .pathogenType(disease.getPathogenType())
                .shortDescription(disease.getShortDescription())
                .description(disease.getDescription())
                .symptoms(disease.getSymptoms())
                .causes(disease.getCauses())
                .favorableConditions(disease.getFavorableConditions())
                .preventionMethod(disease.getPreventionMethod())
                .treatmentGuideline(disease.getTreatmentGuideline())
                .thumbnailUrl(disease.getThumbnailUrl())
                .reviewStatus(disease.getReviewStatus().getValue())
                .rejectionReason(disease.getRejectionReason())
                .submittedAt(disease.getSubmittedAt())
                .reviewedAt(disease.getReviewedAt())
                .reviewedBy(disease.getReviewedBy())
                .publishedAt(disease.getPublishedAt())
                .createdAt(disease.getCreatedAt())
                .createdBy(disease.getCreatedBy())
                .updatedAt(disease.getUpdatedAt())
                .updatedBy(disease.getUpdatedBy())
                .deletedAt(disease.getDeletedAt())
                .deletedBy(disease.getDeletedBy())
                .build();
    }
}
