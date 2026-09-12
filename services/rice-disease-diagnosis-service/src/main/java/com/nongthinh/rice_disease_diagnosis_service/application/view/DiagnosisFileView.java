package com.nongthinh.rice_disease_diagnosis_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.application.model.DiagnosisFileMetadata;

public record DiagnosisFileView(
        UUID id,
        String originalFileName,
        String contentType,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt
) {
    public static DiagnosisFileView from(DiagnosisFileMetadata file) {
        return new DiagnosisFileView(
                file.id(), file.originalFileName(), file.contentType(), file.sizeBytes(),
                file.createdAt(), file.updatedAt());
    }
}
