package com.nongthinh.agri_catalog_service.application.model;

import java.util.UUID;

public record FileMetadata(
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
    public boolean isActiveProductImage() {
        return "PRODUCT_IMAGE".equalsIgnoreCase(purpose)
                && "UPLOADED".equalsIgnoreCase(status)
                && contentType != null
                && contentType.toLowerCase().startsWith("image/")
                && publicUrl != null
                && !publicUrl.isBlank();
    }

    public boolean isActiveModelArtifact() {
        return "MODEL_ARTIFACT".equalsIgnoreCase(purpose)
                && "UPLOADED".equalsIgnoreCase(status)
                && "PRIVATE".equalsIgnoreCase(visibility)
                && originalFileName != null
                && originalFileName.toLowerCase(java.util.Locale.ROOT).endsWith(".onnx")
                && sizeBytes > 0;
    }
}
