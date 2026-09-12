package com.nongthinh.profile_service.infra.persistence.farmerprofile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.farmerprofile.valueobject.Gender;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;

@Mapper(componentModel = "spring")
public interface FarmerProfilePersistenceMapper {

    default FarmerProfile toDomain(JpaFarmerProfileEntity entity) {
        return FarmerProfile.reconstruct(
            entity.getId(),
            entity.getUserId(),
            PersonName.of(entity.getFirstName()),
            PersonName.of(entity.getLastName()),
            Gender.fromString(entity.getGender()),
            entity.getPhone(),
            Address.of(entity.getProvinceId(), entity.getCommuneId(), entity.getAddressDetail()),
            entity.getAvatarUrl(),
            StandardProfileStatus.fromString(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "personNameToString")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "personNameToString")
    @Mapping(target = "gender", source = "gender", qualifiedByName = "genderToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "standardStatusToString")
    @Mapping(target = "provinceId", source = "address.provinceId")
    @Mapping(target = "communeId", source = "address.communeId")
    @Mapping(target = "addressDetail", source = "address.detail")
    JpaFarmerProfileEntity toEntity(FarmerProfile farmerProfile);

    @Named("personNameToString")
    default String personNameToString(PersonName personName) {
        return personName.getValue();
    }

    @Named("genderToString")
    default String genderToString(Gender gender) {
        return gender.getValue();
    }

    @Named("standardStatusToString")
    default String standardStatusToString(StandardProfileStatus status) {
        return status.getValue();
    }
}
