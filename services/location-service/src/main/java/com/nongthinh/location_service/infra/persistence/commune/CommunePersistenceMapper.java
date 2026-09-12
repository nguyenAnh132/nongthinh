package com.nongthinh.location_service.infra.persistence.commune;

import org.mapstruct.Mapper;
import com.nongthinh.location_service.domain.commune.Commune;

@Mapper(componentModel = "spring")
public interface CommunePersistenceMapper {

    default Commune toDomain(JpaCommuneEntity entity) {
        return Commune.reconstruct(
            entity.getId(),
            entity.getProvinceId(),
            entity.getCode(),
            entity.getName()
        );
    }

    JpaCommuneEntity toEntity(Commune commune);
}
