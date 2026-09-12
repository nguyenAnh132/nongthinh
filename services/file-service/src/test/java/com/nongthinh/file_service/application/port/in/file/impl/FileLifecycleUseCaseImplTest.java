package com.nongthinh.file_service.application.port.in.file.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.application.port.out.ClockProvider;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.common.currentuser.CurrentUser;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;

class FileLifecycleUseCaseImplTest {

    private static final UUID OWNER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID FILE_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT = Instant.parse("2026-08-09T00:00:00Z");
    private static final Instant DELETED_AT = Instant.parse("2026-08-09T01:00:00Z");

    private final StoredFileRepository storedFileRepository = mock(StoredFileRepository.class);
    private final ObjectStoragePort objectStoragePort = mock(ObjectStoragePort.class);
    private final ClockProvider clockProvider = mock(ClockProvider.class);

    @Test
    void publicAccessDoesNotExposePrivateDiagnosisImage() {
        StoredFile diagnosisImage = file(FilePurpose.DIAGNOSIS_IMAGE, "field.jpg", "image/jpeg");
        when(storedFileRepository.findById(FILE_ID)).thenReturn(Optional.of(diagnosisImage));

        GetActiveFileForPublicAccessUseCaseImpl useCase = new GetActiveFileForPublicAccessUseCaseImpl(
                storedFileRepository
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(FILE_ID));

        assertEquals(ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void internalHistoryDeletionRemovesDiagnosisObjectAndSoftDeletesMetadata() {
        StoredFile diagnosisImage = file(FilePurpose.DIAGNOSIS_IMAGE, "field.jpg", "image/jpeg");
        when(storedFileRepository.findById(FILE_ID)).thenReturn(Optional.of(diagnosisImage));
        when(clockProvider.now()).thenReturn(DELETED_AT);
        DeleteDiagnosisImageInternalUseCaseImpl useCase = new DeleteDiagnosisImageInternalUseCaseImpl(
                storedFileRepository,
                objectStoragePort,
                clockProvider
        );

        useCase.execute(FILE_ID);

        assertFalse(diagnosisImage.isActive());
        assertEquals(DELETED_AT, diagnosisImage.getUpdatedAt());
        verify(objectStoragePort).deleteObject("bucket", "diagnosis-images/field.jpg");
        verify(storedFileRepository).save(diagnosisImage);
    }

    @Test
    void internalHistoryDeletionRejectsAnyNonDiagnosisFile() {
        StoredFile avatar = file(FilePurpose.AVATAR, "avatar.jpg", "image/jpeg");
        when(storedFileRepository.findById(FILE_ID)).thenReturn(Optional.of(avatar));
        DeleteDiagnosisImageInternalUseCaseImpl useCase = new DeleteDiagnosisImageInternalUseCaseImpl(
                storedFileRepository,
                objectStoragePort,
                clockProvider
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(FILE_ID));

        assertEquals(ErrorCode.FILE_PURPOSE_MISMATCH, exception.getErrorCode());
        verifyNoInteractions(objectStoragePort, clockProvider);
    }

    @Test
    void internalHistoryDeletionIsIdempotentWhenTheFileWasAlreadyRemoved() {
        when(storedFileRepository.findById(FILE_ID)).thenReturn(Optional.empty());
        DeleteDiagnosisImageInternalUseCaseImpl useCase = new DeleteDiagnosisImageInternalUseCaseImpl(
                storedFileRepository,
                objectStoragePort,
                clockProvider
        );

        useCase.execute(FILE_ID);

        verifyNoInteractions(objectStoragePort, clockProvider);
    }

    @Test
    void managedFilesCannotBeDeletedThroughTheUserEndpoint() {
        StoredFile diagnosisImage = file(FilePurpose.DIAGNOSIS_IMAGE, "field.jpg", "image/jpeg");
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        when(storedFileRepository.findById(FILE_ID)).thenReturn(Optional.of(diagnosisImage));
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                OWNER_ID,
                "keycloak-id",
                "farmer@nongthinh.vn",
                Set.of("ROLE_FARMER"),
                Set.of()
        ));
        DeleteFileUseCaseImpl useCase = new DeleteFileUseCaseImpl(
                currentUserProvider,
                storedFileRepository,
                objectStoragePort,
                clockProvider
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.execute(FILE_ID));

        assertEquals(ErrorCode.FILE_MANAGED_LIFECYCLE, exception.getErrorCode());
        verifyNoInteractions(objectStoragePort, clockProvider);
    }

    private StoredFile file(FilePurpose purpose, String name, String contentType) {
        return StoredFile.create(
                FILE_ID,
                OWNER_ID,
                purpose,
                StorageProvider.LOCAL,
                "bucket",
                purpose == FilePurpose.DIAGNOSIS_IMAGE ? "diagnosis-images/field.jpg" : "avatars/avatar.jpg",
                name,
                contentType,
                4,
                "http://localhost/public/" + FILE_ID,
                CREATED_AT
        );
    }
}
