package com.nongthinh.profile_service.infra.persistence.adminprofile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;

@Mapper(componentModel = "spring")
public interface AdminProfilePersistenceMapper {

    default AdminProfile toDomain(JpaAdminProfileEntity entity) {
        return AdminProfile.reconstruct(
            entity.getId(),
            entity.getUserId(),
            PersonName.of(entity.getFirstName()),
            PersonName.of(entity.getLastName()),
            entity.getPhone(),
            entity.getAvatarUrl(),
            StandardProfileStatus.fromString(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "personNameToString")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "personNameToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "standardStatusToString")
    JpaAdminProfileEntity toEntity(AdminProfile adminProfile);

    @Named("personNameToString")
    default String personNameToString(PersonName personName) {
        return personName.getValue();
    }

    @Named("standardStatusToString")
    default String standardStatusToString(StandardProfileStatus status) {
        return status.getValue();
    }
}
