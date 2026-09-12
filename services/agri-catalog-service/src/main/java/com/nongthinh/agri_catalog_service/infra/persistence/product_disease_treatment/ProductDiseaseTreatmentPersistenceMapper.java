package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;

@Mapper(componentModel = "spring")
public interface ProductDiseaseTreatmentPersistenceMapper {

    default ProductDiseaseTreatment toDomain(JpaProductDiseaseTreatmentEntity entity) {
        return ProductDiseaseTreatment.reconstruct(
                entity.getId(),
                entity.getProductId(),
                entity.getDiseaseId(),
                entity.getBrandId(),
                entity.getEffectivenessLevel() == null
                        ? null
                        : EffectivenessLevel.fromString(entity.getEffectivenessLevel()),
                entity.getPriority(),
                entity.getDosage(),
                entity.getApplicationMethod(),
                entity.getApplicationTiming(),
                entity.getFrequencyInstruction(),
                entity.getTreatmentNote(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaProductDiseaseTreatmentEntity toEntity(
            ProductDiseaseTreatment treatment
    ) {
        return JpaProductDiseaseTreatmentEntity.builder()
                .id(treatment.getId())
                .productId(treatment.getProductId())
                .diseaseId(treatment.getDiseaseId())
                .brandId(treatment.getBrandId())
                .effectivenessLevel(
                        treatment.getEffectivenessLevel() == null
                                ? null
                                : treatment.getEffectivenessLevel().getValue()
                )
                .priority(treatment.getPriority())
                .dosage(treatment.getDosage())
                .applicationMethod(treatment.getApplicationMethod())
                .applicationTiming(treatment.getApplicationTiming())
                .frequencyInstruction(treatment.getFrequencyInstruction())
                .treatmentNote(treatment.getTreatmentNote())
                .createdAt(treatment.getCreatedAt())
                .createdBy(treatment.getCreatedBy())
                .updatedAt(treatment.getUpdatedAt())
                .updatedBy(treatment.getUpdatedBy())
                .deletedAt(treatment.getDeletedAt())
                .deletedBy(treatment.getDeletedBy())
                .build();
    }
}
