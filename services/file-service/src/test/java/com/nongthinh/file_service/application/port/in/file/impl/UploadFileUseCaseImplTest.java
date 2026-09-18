package com.nongthinh.file_service.application.port.in.file.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.application.command.UploadFileCommand;
import com.nongthinh.file_service.application.port.out.ClockProvider;
import com.nongthinh.file_service.application.port.out.IdGenerator;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.common.currentuser.CurrentUser;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.configuration.FileUploadProperties;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.domain.file.FileUploadPolicy;
import com.nongthinh.file_service.domain.file.FileValidationPolicy;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.configuration.StorageProperties;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FileVisibility;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;

class UploadFileUseCaseImplTest {

    private static final UUID FARMER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID FILE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant NOW = Instant.parse("2026-08-09T00:00:00Z");

    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final StoredFileRepository storedFileRepository = mock(StoredFileRepository.class);
    private final ObjectStoragePort objectStoragePort = mock(ObjectStoragePort.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);
    private final StorageProperties storageProperties = new StorageProperties();
    private final FileUploadProperties fileUploadProperties = new FileUploadProperties();
    private final FileUploadPolicyProvider policyProvider = mock(FileUploadPolicyProvider.class);
    private final UploadFileUseCaseImpl useCase = new UploadFileUseCaseImpl(
            currentUserProvider,
            storedFileRepository,
            objectStoragePort,
            idGenerator,
            clockProvider,
            storageProperties,
            policyProvider
    );

    @BeforeEach
    void setUp() {
        storageProperties.setBucket("nongthinh-files-test");
        when(policyProvider.getPolicy(any())).thenAnswer(invocation -> {
            FilePurpose purpose = invocation.getArgument(0);
            return new FileUploadPolicy(fileUploadProperties.maxSizeFor(purpose),
                    FileValidationPolicy.supportedContentTypes(purpose));
        });
        when(objectStoragePort.provider()).thenReturn(StorageProvider.LOCAL);
        when(idGenerator.generate()).thenReturn(FILE_ID);
        when(clockProvider.now()).thenReturn(NOW);
        when(storedFileRepository.save(any(StoredFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesDiagnosisImagePrivatelyWithoutPublicUrl() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));

        FileView view = useCase.execute(new UploadFileCommand(
                FARMER_ID,
                "diagnosis_image",
                "field.webp",
                "image/webp",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        ));

        assertEquals(FileVisibility.PRIVATE, view.visibility());
        assertNull(view.publicUrl());
        assertEquals("image/webp", view.contentType());
        verify(objectStoragePort).putObject(
                eq("nongthinh-files-test"),
                eq("diagnosis-images/" + FARMER_ID + "/" + FILE_ID + ".webp"),
                eq("image/webp"),
                eq(4L),
                any()
        );
        verify(objectStoragePort, never()).buildPublicUrl(any());
    }

    @Test
    void storesOnnxArtifactAsPrivateBinaryRegardlessOfBrowserContentType() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(ADMIN_ID, "ROLE_ADMIN"));

        FileView view = useCase.execute(new UploadFileCommand(
                ADMIN_ID,
                "MODEL_ARTIFACT",
                "rice-disease.onnx",
                "image/png",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        ));

        assertEquals(FileVisibility.PRIVATE, view.visibility());
        assertNull(view.publicUrl());
        assertEquals("application/octet-stream", view.contentType());
        verify(objectStoragePort).putObject(
                eq("nongthinh-files-test"),
                eq("model-artifacts/" + ADMIN_ID + "/" + FILE_ID + ".onnx"),
                eq("application/octet-stream"),
                eq(4L),
                any()
        );
    }

    @Test
    void brandCanUploadDiagnosisImageOwnedByItsAccount() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(BRAND_ID, "ROLE_BRAND"));
        FileView view = useCase.execute(new UploadFileCommand(
                BRAND_ID, "DIAGNOSIS_IMAGE", "field.webp", "image/webp", 4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})));

        assertEquals(BRAND_ID, view.ownerUserId());
        assertEquals(FileVisibility.PRIVATE, view.visibility());
        assertNull(view.publicUrl());
        verify(objectStoragePort).putObject(eq("nongthinh-files-test"),
                eq("diagnosis-images/" + BRAND_ID + "/" + FILE_ID + ".webp"),
                eq("image/webp"), eq(4L), any());
        verify(objectStoragePort, never()).buildPublicUrl(any());
    }

    @Test
    void brandCannotUploadDiagnosisImageForAnotherAccount() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(BRAND_ID, "ROLE_BRAND"));
        BusinessException error = assertThrows(BusinessException.class, () -> useCase.execute(new UploadFileCommand(
                FARMER_ID, "DIAGNOSIS_IMAGE", "field.webp", "image/webp", 4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4}))));
        assertEquals(ErrorCode.FILE_ACCESS_DENIED, error.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void farmerCanUploadPublicPostImage() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));
        when(objectStoragePort.buildPublicUrl(any())).thenReturn("http://localhost/public/" + FILE_ID);

        FileView view = useCase.execute(new UploadFileCommand(
                FARMER_ID,
                "POST_IMAGE",
                "rice-field.png",
                "image/png",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        ));

        assertEquals(FileVisibility.PUBLIC, view.visibility());
        assertEquals("http://localhost/public/" + FILE_ID, view.publicUrl());
        verify(objectStoragePort).putObject(
                eq("nongthinh-files-test"),
                eq("post-images/" + FARMER_ID + "/" + FILE_ID + ".png"),
                eq("image/png"),
                eq(4L),
                any()
        );
    }

    @Test
    void brandCanUploadPublicPostVideo() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(BRAND_ID, "ROLE_BRAND"));
        when(objectStoragePort.buildPublicUrl(any())).thenReturn("http://localhost/public/" + FILE_ID);

        FileView view = useCase.execute(new UploadFileCommand(
                BRAND_ID,
                "post_video",
                "harvest.mov",
                "video/quicktime",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        ));

        assertEquals(FileVisibility.PUBLIC, view.visibility());
        assertEquals("video/quicktime", view.contentType());
        verify(objectStoragePort).putObject(
                eq("nongthinh-files-test"),
                eq("post-videos/" + BRAND_ID + "/" + FILE_ID + ".mov"),
                eq("video/quicktime"),
                eq(4L),
                any()
        );
    }

    @Test
    void postVideoRejectsUnsupportedContentTypeBeforeStorage() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(
                        FARMER_ID,
                        "POST_VIDEO",
                        "malware.exe",
                        "application/octet-stream",
                        4,
                        new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
                )));

        assertEquals(ErrorCode.CONTENT_TYPE_NOT_ALLOWED, exception.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void postVideoRejectsFileLargerThanFiftyMebibytesBeforeStorage() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(
                        FARMER_ID,
                        "POST_VIDEO",
                        "harvest.mp4",
                        "video/mp4",
                        50L * 1024 * 1024 + 1,
                        new ByteArrayInputStream(new byte[] {1})
                )));

        assertEquals(ErrorCode.FILE_TOO_LARGE, exception.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void adminCannotUploadPostMedia() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(ADMIN_ID, "ROLE_ADMIN"));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(
                        ADMIN_ID,
                        "POST_IMAGE",
                        "announcement.jpg",
                        "image/jpeg",
                        4,
                        new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
                )));

        assertEquals(ErrorCode.FILE_PURPOSE_NOT_ALLOWED, exception.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void farmerCannotUploadModelArtifact() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(new UploadFileCommand(
                FARMER_ID,
                "MODEL_ARTIFACT",
                "rice-disease.onnx",
                "application/octet-stream",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        )));

        assertEquals(ErrorCode.FILE_PURPOSE_NOT_ALLOWED, exception.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void adminCannotUploadDiagnosisImage() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(ADMIN_ID, "ROLE_ADMIN"));

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(new UploadFileCommand(
                ADMIN_ID,
                "DIAGNOSIS_IMAGE",
                "field.png",
                "image/png",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        )));

        assertEquals(ErrorCode.FILE_PURPOSE_NOT_ALLOWED, exception.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void removesObjectWhenMetadataPersistenceFails() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));
        when(storedFileRepository.save(any(StoredFile.class))).thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> useCase.execute(new UploadFileCommand(
                FARMER_ID,
                "DIAGNOSIS_IMAGE",
                "field.webp",
                "image/webp",
                4,
                new ByteArrayInputStream(new byte[] {1, 2, 3, 4})
        )));

        verify(objectStoragePort).deleteObject(
                "nongthinh-files-test",
                "diagnosis-images/" + FARMER_ID + "/" + FILE_ID + ".webp"
        );
    }

    private CurrentUser user(UUID userId, String role) {
        return new CurrentUser(userId, "keycloak-id", "user@nongthinh.vn", Set.of(role), Set.of());
    }

    @Test
    void rejectsFileExceedingConfiguredLimit() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));
        when(policyProvider.getPolicy(FilePurpose.POST_IMAGE))
                .thenReturn(new FileUploadPolicy(3, Set.of("image/png")));
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(FARMER_ID, "POST_IMAGE", "field.png", "image/png", 4,
                        new ByteArrayInputStream(new byte[4]))));
        assertEquals(ErrorCode.FILE_TOO_LARGE, ex.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void allTypesDisabledRejectsUploadIncludingOnnx() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(ADMIN_ID, "ROLE_ADMIN"));
        when(policyProvider.getPolicy(FilePurpose.MODEL_ARTIFACT))
                .thenReturn(new FileUploadPolicy(1024, Set.of()));
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(ADMIN_ID, "MODEL_ARTIFACT", "model.onnx", "application/octet-stream", 4,
                        new ByteArrayInputStream(new byte[4]))));
        assertEquals(ErrorCode.CONTENT_TYPE_NOT_ALLOWED, ex.getErrorCode());
        verify(objectStoragePort, never()).putObject(any(), any(), any(), any(Long.class), any());
    }

    @Test
    void disabledPngIsRejectedWhileJpegRemainsEnabled() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user(FARMER_ID, "ROLE_FARMER"));
        when(policyProvider.getPolicy(FilePurpose.POST_IMAGE))
                .thenReturn(new FileUploadPolicy(1024, Set.of("image/jpeg")));
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(
                new UploadFileCommand(FARMER_ID, "POST_IMAGE", "field.png", "image/png", 4,
                        new ByteArrayInputStream(new byte[4]))));
        assertEquals(ErrorCode.CONTENT_TYPE_NOT_ALLOWED, ex.getErrorCode());
    }
}
