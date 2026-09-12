package com.nongthinh.file_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public interface StoredFileRepository {

    StoredFile save(StoredFile file);

    Optional<StoredFile> findById(UUID id);

    List<StoredFile> findByOwnerUserIdAndPurpose(UUID ownerUserId, FilePurpose purpose);
}
