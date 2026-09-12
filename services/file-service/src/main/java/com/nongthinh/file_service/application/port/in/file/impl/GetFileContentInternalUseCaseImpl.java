package com.nongthinh.file_service.application.port.in.file.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.file_service.application.port.in.file.GetFileContentInternalUseCase;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.StoredFile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFileContentInternalUseCaseImpl implements GetFileContentInternalUseCase {

    private final StoredFileRepository storedFileRepository;

    @Override
    @Transactional(readOnly = true)
    public StoredFile execute(UUID fileId) {
        StoredFile file = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        if (!file.isActive()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        return file;
    }
}
