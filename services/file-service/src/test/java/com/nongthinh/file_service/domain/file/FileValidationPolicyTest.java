package com.nongthinh.file_service.domain.file;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

class FileValidationPolicyTest {

    @Test
    void acceptsSupportedPostMediaContentTypes() {
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_IMAGE, "image/jpeg"));
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_IMAGE, "image/png"));
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_IMAGE, "image/webp"));
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_VIDEO, "video/mp4"));
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_VIDEO, "video/webm"));
        assertDoesNotThrow(() -> FileValidationPolicy.assertValidContentType(
                FilePurpose.POST_VIDEO, "video/quicktime"));
    }

    @Test
    void rejectsMediaTypeThatDoesNotMatchPostPurpose() {
        BusinessException imageException = assertThrows(BusinessException.class,
                () -> FileValidationPolicy.assertValidContentType(FilePurpose.POST_IMAGE, "video/mp4"));
        BusinessException videoException = assertThrows(BusinessException.class,
                () -> FileValidationPolicy.assertValidContentType(FilePurpose.POST_VIDEO, "image/png"));

        assertEquals(ErrorCode.CONTENT_TYPE_NOT_ALLOWED, imageException.getErrorCode());
        assertEquals(ErrorCode.CONTENT_TYPE_NOT_ALLOWED, videoException.getErrorCode());
    }

    @Test
    void resolvesVideoExtensionFromValidatedContentType() {
        assertEquals("mp4", FileValidationPolicy.resolveExtension(
                FilePurpose.POST_VIDEO, "video/mp4", "untrusted.exe"));
        assertEquals("webm", FileValidationPolicy.resolveExtension(
                FilePurpose.POST_VIDEO, "video/webm", "untrusted.exe"));
        assertEquals("mov", FileValidationPolicy.resolveExtension(
                FilePurpose.POST_VIDEO, "video/quicktime", "untrusted.exe"));
    }
}
