package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import org.springframework.stereotype.Component;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurpose;
import com.nongthinh.bo_portal_service.domain.fileconfig.FilePurposeType;
import com.nongthinh.bo_portal_service.domain.fileconfig.FileType;

@Component
public class FilePurposeTypePersistenceMapper {
    public FileType toDomain(JpaFileTypeEntity entity) {
        return new FileType(entity.getCode(), entity.getContentType(), entity.getExtension());
    }

    public FilePurposeType toDomain(JpaFilePurposeTypeEntity entity) {
        return FilePurposeType.reconstruct(entity.getId(), FilePurpose.valueOf(entity.getPurpose()),
                toDomain(entity.getFileType()), entity.isEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public JpaFilePurposeTypeEntity toEntity(FilePurposeType domain, JpaFileTypeEntity type) {
        var entity = new JpaFilePurposeTypeEntity();
        entity.setId(domain.getId());
        entity.setPurpose(domain.getPurpose().name());
        entity.setFileType(type);
        entity.setEnabled(domain.isEnabled());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
