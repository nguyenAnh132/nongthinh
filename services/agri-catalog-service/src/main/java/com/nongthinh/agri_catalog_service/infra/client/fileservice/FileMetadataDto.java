package com.nongthinh.agri_catalog_service.infra.client.fileservice;

import java.util.UUID;

public record FileMetadataDto(
        UUID id,
        UUID ownerUserId,
        String purpose,
        String originalFileName,
        String contentType,
        long sizeBytes,
        String publicUrl,
        String visibility,
        String status
) {
}
