package com.nongthinh.file_service.application.port.in.file.impl;

import java.util.Objects;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.file_service.application.command.UploadFileCommand;
import com.nongthinh.file_service.application.port.in.file.UploadFileUseCase;
import com.nongthinh.file_service.application.port.out.ClockProvider;
import com.nongthinh.file_service.application.port.out.IdGenerator;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.common.currentuser.CurrentUser;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.configuration.StorageProperties;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.FileAccessPolicy;
import com.nongthinh.file_service.domain.file.FileValidationPolicy;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FileVisibility;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadFileUseCaseImpl implements UploadFileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final StoredFileRepository storedFileRepository;
    private final ObjectStoragePort objectStoragePort;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final StorageProperties storageProperties;
    private final FileUploadPolicyProvider fileUploadPolicyProvider;

    @Override
    @Transactional
    public FileView execute(UploadFileCommand command) {
        Objects.requireNonNull(command, "command is required");

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (!currentUser.getUserId().equals(command.ownerUserId())) {
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
        }

        FilePurpose purpose = FilePurpose.from(command.purpose());
        String contentType = normalizeContentType(purpose, command.contentType());

        FileAccessPolicy.assertCanUpload(currentUser, purpose);
        FileValidationPolicy.assertValidFileName(purpose, command.originalFileName());
        var uploadPolicy = fileUploadPolicyProvider.getPolicy(purpose);
        FileValidationPolicy.assertValidContentType(purpose, contentType, uploadPolicy.allowedContentTypes());
        FileValidationPolicy.assertValidSize(purpose, command.sizeBytes(), uploadPolicy.maxSizeBytes());

        var fileId = idGenerator.generate();
        String extension = FileValidationPolicy.resolveExtension(purpose, contentType, command.originalFileName());
        String objectKey = buildObjectKey(purpose, command.ownerUserId(), fileId, extension);
        String bucket = storageProperties.getBucket();

        try {
            objectStoragePort.putObject(
                    bucket,
                    objectKey,
                    contentType,
                    command.sizeBytes(),
                    command.inputStream());
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE, ex);
        }

        String publicUrl = FileVisibility.forPurpose(purpose).isPublic()
                ? objectStoragePort.buildPublicUrl(objectKey)
                : null;
        StoredFile storedFile = StoredFile.create(
                fileId,
                command.ownerUserId(),
                purpose,
                objectStoragePort.provider(),
                bucket,
                objectKey,
                command.originalFileName(),
                contentType,
                command.sizeBytes(),
                publicUrl,
                clockProvider.now());

        try {
            return FileView.from(storedFileRepository.save(storedFile));
        } catch (RuntimeException persistenceException) {
            removeStoredObjectAfterPersistenceFailure(bucket, objectKey, persistenceException);
            throw persistenceException;
        }
    }

    private String buildObjectKey(
            FilePurpose purpose,
            UUID ownerUserId,
            UUID fileId,
            String extension) {
        String folder = switch (purpose) {
            case AVATAR -> "avatars";
            case BRAND_LOGO -> "brand-logos";
            case BRAND_BANNER -> "brand-banners";
            case BUSINESS_LICENSE -> "business-licenses";
            case PRODUCT_IMAGE -> "product-images";
            case DISEASE_IMAGE -> "disease-images";
            case DIAGNOSIS_IMAGE -> "diagnosis-images";
            case POST_IMAGE -> "post-images";
            case POST_VIDEO -> "post-videos";
            case MODEL_ARTIFACT -> "model-artifacts";
        };
        return folder + "/" + ownerUserId + "/" + fileId + "." + extension;
    }

    private String normalizeContentType(FilePurpose purpose, String contentType) {
        if (purpose == FilePurpose.MODEL_ARTIFACT) {
            return "application/octet-stream";
        }
        return contentType;
    }

    private void removeStoredObjectAfterPersistenceFailure(
            String bucket,
            String objectKey,
            RuntimeException persistenceException) {
        try {
            objectStoragePort.deleteObject(bucket, objectKey);
        } catch (RuntimeException cleanupException) {
            persistenceException.addSuppressed(cleanupException);
        }
    }
}
