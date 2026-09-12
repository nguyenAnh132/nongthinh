package com.nongthinh.bo_portal_service.infra.persistence.systemparam;

import com.nongthinh.bo_portal_service.domain.systemparam.SystemParam;
import com.nongthinh.bo_portal_service.domain.systemparam.SystemParamDataType;
import org.springframework.stereotype.Component;

@Component
public class SystemParamPersistenceMapper {

    public SystemParam toDomain(JpaSystemParamEntity entity) {
        return SystemParam.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getValue(),
                entity.getDescription(),
                SystemParamDataType.from(entity.getDataType()),
                entity.isSystemDefined(),
                entity.getTypeId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public JpaSystemParamEntity toEntity(SystemParam systemParam) {
        JpaSystemParamEntity entity = new JpaSystemParamEntity();
        entity.setId(systemParam.getId());
        entity.setName(systemParam.getName());
        entity.setValue(systemParam.getValue());
        entity.setDescription(systemParam.getDescription());
        entity.setDataType(systemParam.getDataType().name());
        entity.setSystemDefined(systemParam.isSystemDefined());
        entity.setTypeId(systemParam.getTypeId());
        entity.setCreatedAt(systemParam.getCreatedAt());
        entity.setUpdatedAt(systemParam.getUpdatedAt());
        return entity;
    }
}
