package com.nongthinh.profile_service.infra.persistence.branddocument;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.nongthinh.profile_service.domain.branddocument.BrandDocument;
import com.nongthinh.profile_service.domain.branddocument.valueobject.BrandDocumentReviewStatus;

@Mapper(componentModel = "spring")
public interface BrandDocumentPersistenceMapper {

    default BrandDocument toDomain(JpaBrandDocumentEntity entity) {
        return BrandDocument.reconstruct(
            entity.getId(),
            entity.getBrandProfileId(),
            entity.getBusinessLicenseUrl(),
            BrandDocumentReviewStatus.fromString(entity.getReviewStatus()),
            entity.getReviewedBy(),
            entity.getReviewedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Mapping(target = "reviewStatus", source = "reviewStatus", qualifiedByName = "reviewStatusToString")
    JpaBrandDocumentEntity toEntity(BrandDocument brandDocument);

    @Named("reviewStatusToString")
    default String reviewStatusToString(BrandDocumentReviewStatus reviewStatus) {
        return reviewStatus.getValue();
    }
}
