package com.nongthinh.auth_service.infra.client.profileservice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.infra.client.profileservice.dto.AdminProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.BrandProfileDto;
import com.nongthinh.auth_service.infra.client.profileservice.dto.FarmerProfileDto;

@Component
@Mapper(componentModel = "spring")
public interface ProfileViewMapper {

    String TYPE_FARMER = "FARMER";
    String TYPE_BRAND = "BRAND";
    String TYPE_ADMIN = "ADMIN";

    @Mapping(target = "displayName", expression = "java(joinName(profile.firstName(), profile.lastName()))")
    @Mapping(target = "profileId", source = "id")
    @Mapping(target = "type", constant = TYPE_FARMER)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "scheduledDeletionAt", ignore = true)
    ProfileView fromFarmer(FarmerProfileDto profile);

    @Mapping(target = "displayName", source = "brandName")
    @Mapping(target = "profileId", source = "id")
    @Mapping(target = "type", constant = TYPE_BRAND)
    @Mapping(target = "avatarUrl", source = "logoUrl")
    ProfileView fromBrand(BrandProfileDto profile);

    @Mapping(target = "displayName", expression = "java(joinName(profile.firstName(), profile.lastName()))")
    @Mapping(target = "profileId", source = "id")
    @Mapping(target = "type", constant = TYPE_ADMIN)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "scheduledDeletionAt", ignore = true)
    ProfileView fromAdmin(AdminProfileDto profile);

    default String joinName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
