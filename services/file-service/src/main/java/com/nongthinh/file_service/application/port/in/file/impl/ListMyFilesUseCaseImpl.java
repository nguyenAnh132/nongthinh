package com.nongthinh.file_service.application.port.in.file.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.file_service.application.port.in.file.ListMyFilesUseCase;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.application.view.FileView;
import com.nongthinh.file_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.FileStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListMyFilesUseCaseImpl implements ListMyFilesUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final StoredFileRepository storedFileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FileView> execute(String purpose) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        return storedFileRepository.findByOwnerUserIdAndPurpose(userId, FilePurpose.from(purpose))
                .stream()
                .filter(file -> file.getStatus() == FileStatus.UPLOADED)
                .map(FileView::from)
                .toList();
    }
}
