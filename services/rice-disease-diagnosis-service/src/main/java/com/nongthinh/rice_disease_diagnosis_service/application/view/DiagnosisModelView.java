package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.util.UUID;

public record DiagnosisModelView(UUID id, String name, UUID versionId, String version) {
}
