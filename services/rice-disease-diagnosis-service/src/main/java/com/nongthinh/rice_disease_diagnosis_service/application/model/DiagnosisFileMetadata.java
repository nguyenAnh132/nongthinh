package com.nongthinh.rice_disease_diagnosis_service.application.model;

import java.time.Instant;
import java.util.UUID;

public record DiagnosisFileMetadata(
        UUID id,
        UUID ownerUserId,
        String purpose,
        String originalFileName,
        String contentType,
        long sizeBytes,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isActiveDiagnosisImageOwnedBy(UUID farmerUserId) {
        return farmerUserId.equals(ownerUserId)
                && "DIAGNOSIS_IMAGE".equalsIgnoreCase(purpose)
                && "UPLOADED".equalsIgnoreCase(status)
                && contentType != null
                && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("image/");
    }
}
