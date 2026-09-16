package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import com.nongthinh.bo_portal_service.application.port.out.repository.FilePurposeTypeRepository;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurpose;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurposeType;
import com.nongthinh.bo_portal_service.domain.fileconfig.FileType;

@Repository
@RequiredArgsConstructor
public class FilePurposeTypeRepositoryImpl implements FilePurposeTypeRepository {
    private final JpaFilePurposeTypeRepository repository;
    private final JpaFileTypeRepository fileTypeRepository;
    private final FilePurposeTypePersistenceMapper mapper;

    @Override
    public List<FileType> findAllFileTypes() {
        return fileTypeRepository.findAllByOrderByCodeAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<FilePurposeType> findByPurpose(FilePurpose purpose) {
        return repository.findByPurposeOrderByFileTypeCodeAsc(purpose.name()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<FilePurposeType> findByPurposeAndTypeCode(FilePurpose purpose, String typeCode) {
        return repository.findByPurposeAndFileTypeCode(purpose.name(), typeCode).map(mapper::toDomain);
    }

    @Override
    public FilePurposeType save(FilePurposeType mapping) {
        var type = fileTypeRepository.getReferenceById(mapping.getFileType().code());
        return mapper.toDomain(repository.save(mapper.toEntity(mapping, type)));
    }
}
