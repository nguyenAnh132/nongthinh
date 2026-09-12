package com.nongthinh.agri_catalog_service.application.command;

import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;

public record ProductDiseaseTreatmentUpdateCommand(
        EffectivenessLevel effectivenessLevel,
        int priority,
        String dosage,
        String applicationMethod,
        String applicationTiming,
        String frequencyInstruction,
        String treatmentNote
) {
}
