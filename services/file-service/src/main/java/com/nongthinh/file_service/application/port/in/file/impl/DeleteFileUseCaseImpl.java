package com.nongthinh.file_service.application.port.in.file.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.file_service.application.port.in.file.DeleteFileUseCase;
import com.nongthinh.file_service.application.port.out.ClockProvider;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.FileAccessPolicy;
import com.nongthinh.file_service.domain.file.StoredFile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteFileUseCaseImpl implements DeleteFileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final StoredFileRepository storedFileRepository;
    private final ObjectStoragePort objectStoragePort;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID fileId) {
        StoredFile file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        FileAccessPolicy.assertCanDelete(currentUserProvider.getCurrentUser(), file);

        if (!file.isActive()) {
            throw new BusinessException(ErrorCode.FILE_ALREADY_DELETED);
        }

        try {
            objectStoragePort.deleteObject(file.getBucket(), file.getObjectKey());
        } catch (RuntimeException ex) {
            throw new BusinessException(ErrorCode.STORAGE_UNAVAILABLE, ex);
        }

        file.markDeleted(clockProvider.now());
        storedFileRepository.save(file);
    }
}
