package com.nongthinh.file_service.domain.file;

import java.util.Map;
import java.util.Locale;
import java.util.Set;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public final class FileValidationPolicy {

    private static final Map<FilePurpose, Set<String>> ALLOWED_CONTENT_TYPES = Map.of(
            FilePurpose.AVATAR, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.BRAND_LOGO, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.BRAND_BANNER, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.BUSINESS_LICENSE,
            Set.of("image/jpeg", "image/png", "image/webp", "application/pdf"),
            FilePurpose.PRODUCT_IMAGE, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.DISEASE_IMAGE, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.DIAGNOSIS_IMAGE, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.POST_IMAGE, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.POST_VIDEO, Set.of("video/mp4", "video/webm", "video/quicktime"),
            FilePurpose.MODEL_ARTIFACT, Set.of());

    private FileValidationPolicy() {
    }

    public static void assertValidContentType(FilePurpose purpose, String contentType) {
        if (purpose == FilePurpose.MODEL_ARTIFACT) {
            return;
        }
        Set<String> allowed = ALLOWED_CONTENT_TYPES.get(purpose);
        if (contentType == null || !allowed.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.CONTENT_TYPE_NOT_ALLOWED);
        }
    }

    public static void assertValidFileName(FilePurpose purpose, String originalFileName) {
        if (purpose == FilePurpose.MODEL_ARTIFACT
                && (originalFileName == null || !originalFileName.toLowerCase(Locale.ROOT).endsWith(".onnx"))) {
            throw new BusinessException(ErrorCode.FILE_NAME_NOT_ALLOWED);
        }
    }

    public static void assertValidSize(FilePurpose purpose, long sizeBytes, long maxSizeBytes) {
        if (sizeBytes <= 0) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (sizeBytes > maxSizeBytes) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    public static String resolveExtension(String contentType, String originalFileName) {
        if (contentType != null) {
            return switch (contentType.toLowerCase()) {
                case "image/jpeg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "video/mp4" -> "mp4";
                case "video/webm" -> "webm";
                case "video/quicktime" -> "mov";
                case "application/pdf" -> "pdf";
                default -> extractExtensionFromFileName(originalFileName);
            };
        }
        return extractExtensionFromFileName(originalFileName);
    }

    public static String resolveExtension(FilePurpose purpose, String contentType, String originalFileName) {
        if (purpose == FilePurpose.MODEL_ARTIFACT) {
            return "onnx";
        }
        return resolveExtension(contentType, originalFileName);
    }

    private static String extractExtensionFromFileName(String originalFileName) {
        if (originalFileName == null) {
            return "bin";
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return "bin";
        }
        return originalFileName.substring(dotIndex + 1).toLowerCase();
    }
}
