package com.nongthinh.agri_catalog_service.infra.client.diagnosis;

public record DiagnosisValidationResponse(
        boolean valid,
        String report
) {
}
