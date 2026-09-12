package com.nongthinh.profile_service.infra.persistence.brandverificationlog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.brandverificationlog.BrandVerificationLog;
import com.nongthinh.profile_service.domain.brandverificationlog.valueobject.VerificationResult;

@Mapper(componentModel = "spring")
public interface BrandVerificationLogPersistenceMapper {

    default BrandVerificationLog toDomain(JpaBrandVerificationLogEntity entity) {
        return BrandVerificationLog.reconstruct(
            entity.getId(),
            entity.getBrandProfileId(),
            entity.getAdminUserId(),
            entity.getPhoneCalled(),
            VerificationResult.fromString(entity.getResult()),
            entity.getNote(),
            entity.getVerifiedAt(),
            entity.getCreatedAt()
        );
    }

    @Mapping(target = "result", source = "result", qualifiedByName = "verificationResultToString")
    JpaBrandVerificationLogEntity toEntity(BrandVerificationLog brandVerificationLog);

    @Named("verificationResultToString")
    default String verificationResultToString(VerificationResult result) {
        return result.getValue();
    }
}
