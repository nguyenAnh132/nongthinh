package com.nongthinh.rice_disease_diagnosis_service.infra.client.fileservice;

import java.time.Instant;
import java.util.UUID;

public record FileMetadataDto(
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
}
