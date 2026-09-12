package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamType;
import org.springframework.stereotype.Component;

@Component
public class SystemParamTypePersistenceMapper {

    public SystemParamType toDomain(JpaSystemParamTypeEntity entity) {
        return SystemParamType.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystemDefined(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public JpaSystemParamTypeEntity toEntity(SystemParamType systemParamType) {
        JpaSystemParamTypeEntity entity = new JpaSystemParamTypeEntity();
        entity.setId(systemParamType.getId());
        entity.setName(systemParamType.getName());
        entity.setDescription(systemParamType.getDescription());
        entity.setSystemDefined(systemParamType.isSystemDefined());
        entity.setCreatedAt(systemParamType.getCreatedAt());
        entity.setUpdatedAt(systemParamType.getUpdatedAt());
        return entity;
    }
}
