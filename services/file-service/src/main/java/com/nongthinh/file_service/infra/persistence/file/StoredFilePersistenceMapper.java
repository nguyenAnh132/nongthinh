package com.nongthinh.file_service.infra.persistence.file;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.StoredFile;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.FileStatus;
import com.nongthinh.file_service.domain.file.valueobject.FileVisibility;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StoredFilePersistenceMapper {

    default StoredFile toDomain(JpaStoredFileEntity entity) {
        return StoredFile.reconstruct(
                entity.getId(),
                entity.getOwnerUserId(),
                FilePurpose.valueOf(entity.getPurpose()),
                StorageProvider.valueOf(entity.getStorageProvider()),
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getOriginalFileName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getPublicUrl(),
                FileVisibility.valueOf(entity.getVisibility()),
                FileStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    default JpaStoredFileEntity toEntity(StoredFile file) {
        JpaStoredFileEntity entity = new JpaStoredFileEntity();
        entity.setId(file.getId());
        entity.setOwnerUserId(file.getOwnerUserId());
        entity.setPurpose(file.getPurpose().name());
        entity.setStorageProvider(file.getStorageProvider().name());
        entity.setBucket(file.getBucket());
        entity.setObjectKey(file.getObjectKey());
        entity.setOriginalFileName(file.getOriginalFileName());
        entity.setContentType(file.getContentType());
        entity.setSizeBytes(file.getSizeBytes());
        entity.setPublicUrl(file.getPublicUrl());
        entity.setVisibility(file.getVisibility().name());
        entity.setStatus(file.getStatus().name());
        entity.setCreatedAt(file.getCreatedAt());
        entity.setUpdatedAt(file.getUpdatedAt());
        return entity;
    }
}
