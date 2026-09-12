package com.nongthinh.agri_catalog_service.application.model;

public record AiModelValidationResult(
        boolean valid,
        String report
) {
}
