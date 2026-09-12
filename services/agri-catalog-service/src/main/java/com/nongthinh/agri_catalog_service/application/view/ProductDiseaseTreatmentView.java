package com.nongthinh.agri_catalog_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;

public record ProductDiseaseTreatmentView(
        UUID id,
        UUID productId,
        UUID diseaseId,
        UUID brandId,
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static ProductDiseaseTreatmentView from(ProductDiseaseTreatment treatment) {
        return new ProductDiseaseTreatmentView(
                treatment.getId(),
                treatment.getProductId(),
                treatment.getDiseaseId(),
                treatment.getBrandId(),
                treatment.getEffectivenessLevel(),
                treatment.getPriority(),
                treatment.getDosage(),
                treatment.getApplicationMethod(),
                treatment.getApplicationTiming(),
                treatment.getFrequencyInstruction(),
                treatment.getTreatmentNote(),
                treatment.getCreatedAt(),
                treatment.getCreatedBy(),
                treatment.getUpdatedAt(),
                treatment.getUpdatedBy()
        );
    }
}
