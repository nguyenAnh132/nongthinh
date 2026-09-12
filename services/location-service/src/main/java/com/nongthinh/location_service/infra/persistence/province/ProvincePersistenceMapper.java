package com.nongthinh.location_service.infra.persistence.province;

import org.mapstruct.Mapper;
import com.nongthinh.location_service.domain.province.Province;

@Mapper(componentModel = "spring")
public interface ProvincePersistenceMapper {

    default Province toDomain(JpaProvinceEntity entity) {
        return Province.reconstruct(entity.getId(), entity.getCode(), entity.getName());
    }

    default JpaProvinceEntity toEntity(Province province) {
        return new JpaProvinceEntity(province.getId(), province.getCode(), province.getName());
    }
}
