package com.nongthinh.file_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.FileStatus;
import com.nongthinh.file_service.domain.file.valueobject.FileVisibility;

public record FileView(
        UUID id,
        UUID ownerUserId,
        FilePurpose purpose,
        String originalFileName,
        String contentType,
        long sizeBytes,
        String publicUrl,
        FileVisibility visibility,
        FileStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static FileView from(StoredFile file) {
        return new FileView(
                file.getId(),
                file.getOwnerUserId(),
                file.getPurpose(),
                file.getOriginalFileName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getPublicUrl(),
                file.getVisibility(),
                file.getStatus(),
                file.getCreatedAt(),
                file.getUpdatedAt());
    }
}
