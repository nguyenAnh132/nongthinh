package com.nongthinh.profile_service.infra.persistence.brandprofile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandName;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;

@Mapper(componentModel = "spring")
public interface BrandProfilePersistenceMapper {

    default BrandProfile toDomain(JpaBrandProfileEntity entity) {
        return BrandProfile.reconstruct(
            entity.getId(),
            entity.getUserId(),
            BrandName.of(entity.getBrandName()),
            entity.getTaxCode(),
            entity.getDescription(),
            entity.getPhone(),
            Address.of(entity.getOfficeProvinceId(), entity.getOfficeCommuneId(), entity.getOfficeAddressDetail()),
            entity.getRepresentativeName(),
            entity.getRepresentativePhone(),
            entity.getRepresentativeEmail(),
            entity.getLogoUrl(),
            entity.getBannerUrl(),
            entity.getWebsiteUrl(),
            BrandProfileStatus.fromString(entity.getStatus()),
            entity.getRejectionReason(),
            entity.getScheduledDeletionAt(),
            entity.getRejectedAt(),
            entity.getApprovedAt(),
            entity.getApprovedBy(),
            entity.getRejectedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Mapping(target = "brandName", source = "brandName", qualifiedByName = "brandNameToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "brandStatusToString")
    @Mapping(target = "officeProvinceId", source = "officeAddress.provinceId")
    @Mapping(target = "officeCommuneId", source = "officeAddress.communeId")
    @Mapping(target = "officeAddressDetail", source = "officeAddress.detail")
    JpaBrandProfileEntity toEntity(BrandProfile brandProfile);

    @Named("brandNameToString")
    default String brandNameToString(BrandName brandName) {
        return brandName.getValue();
    }

    @Named("brandStatusToString")
    default String brandStatusToString(BrandProfileStatus status) {
        return status.getValue();
    }
}
