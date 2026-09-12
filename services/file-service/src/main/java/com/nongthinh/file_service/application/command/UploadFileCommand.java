package com.nongthinh.file_service.application.command;

import java.io.InputStream;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public record UploadFileCommand(
        UUID ownerUserId,
        String purpose,
        String originalFileName,
        String contentType,
        long sizeBytes,
        InputStream inputStream) {
}
