package com.nongthinh.profile_service.infra.persistence.brandlifecyclelog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;

@Mapper(componentModel = "spring")
public interface BrandLifecycleLogPersistenceMapper {

    default BrandLifecycleLog toDomain(JpaBrandLifecycleLogEntity entity) {
        return BrandLifecycleLog.reconstruct(
            entity.getId(),
            entity.getBrandProfileId(),
            BrandLifecycleAction.fromString(entity.getAction()),
            entity.getActorUserId(),
            toBrandProfileStatus(entity.getFromStatus()),
            toBrandProfileStatus(entity.getToStatus()),
            entity.getPayloadJson(),
            entity.getCreatedAt()
        );
    }

    @Mapping(target = "action", source = "action", qualifiedByName = "actionToString")
    @Mapping(target = "fromStatus", source = "fromStatus", qualifiedByName = "brandStatusToString")
    @Mapping(target = "toStatus", source = "toStatus", qualifiedByName = "brandStatusToString")
    JpaBrandLifecycleLogEntity toEntity(BrandLifecycleLog brandLifecycleLog);

    @Named("actionToString")
    default String actionToString(BrandLifecycleAction action) {
        return action.getValue();
    }

    @Named("brandStatusToString")
    default String brandStatusToString(BrandProfileStatus status) {
        return status != null ? status.getValue() : null;
    }

    default BrandProfileStatus toBrandProfileStatus(String raw) {
        return raw != null ? BrandProfileStatus.fromString(raw) : null;
    }
}
