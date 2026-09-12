package com.nongthinh.file_service.application.port.in.file.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.file_service.application.port.in.file.DeleteDiagnosisImageInternalUseCase;
import com.nongthinh.file_service.application.port.out.ClockProvider;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteDiagnosisImageInternalUseCaseImpl implements DeleteDiagnosisImageInternalUseCase {

    private final StoredFileRepository storedFileRepository;
    private final ObjectStoragePort objectStoragePort;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public void execute(UUID fileId) {
        StoredFile file = storedFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return;
        }

        if (file.getPurpose() != FilePurpose.DIAGNOSIS_IMAGE) {
            throw new BusinessException(ErrorCode.FILE_PURPOSE_MISMATCH);
        }
        if (!file.isActive()) {
            return;
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
