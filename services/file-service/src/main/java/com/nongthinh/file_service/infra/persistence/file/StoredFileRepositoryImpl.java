package com.nongthinh.file_service.infra.persistence.file;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.file_service.application.port.out.repository.StoredFileRepository;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoredFileRepositoryImpl implements StoredFileRepository {

    private final JpaStoredFileRepository jpaStoredFileRepository;
    private final StoredFilePersistenceMapper storedFilePersistenceMapper;

    @Override
    public StoredFile save(StoredFile file) {
        JpaStoredFileEntity entity = storedFilePersistenceMapper.toEntity(file);
        JpaStoredFileEntity saved = jpaStoredFileRepository.save(entity);
        return storedFilePersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<StoredFile> findById(UUID id) {
        return jpaStoredFileRepository.findById(id)
                .map(storedFilePersistenceMapper::toDomain);
    }

    @Override
    public List<StoredFile> findByOwnerUserIdAndPurpose(UUID ownerUserId, FilePurpose purpose) {
        return jpaStoredFileRepository
                .findByOwnerUserIdAndPurposeOrderByCreatedAtDesc(ownerUserId, purpose.name())
                .stream()
                .map(storedFilePersistenceMapper::toDomain)
                .toList();
    }
}
