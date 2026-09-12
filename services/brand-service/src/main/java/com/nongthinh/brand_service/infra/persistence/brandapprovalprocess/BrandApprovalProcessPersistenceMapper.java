package com.nongthinh.brand_service.infra.persistence.brandapprovalprocess;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcess;
import com.nongthinh.brand_service.domain.brandapprovalprocess.BrandApprovalProcessStatus;

@Mapper(componentModel = "spring")
public interface BrandApprovalProcessPersistenceMapper {

    default BrandApprovalProcess toDomain(JpaBrandApprovalProcessEntity entity) {
        return BrandApprovalProcess.reconstruct(
                entity.getId(),
                entity.getBrandProfileId(),
                entity.getCamundaProcessInstanceId(),
                entity.getCamundaBusinessKey(),
                BrandApprovalProcessStatus.fromString(entity.getStatus()),
                entity.getAssignedReviewerId(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    JpaBrandApprovalProcessEntity toEntity(BrandApprovalProcess brandApprovalProcess);

    @Named("statusToString")
    default String statusToString(BrandApprovalProcessStatus status) {
        return status.getValue();
    }
}
