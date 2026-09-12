package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.DiseaseReviewHistory;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class DiseaseUseCaseSupport {

    private final DiseaseRepository diseaseRepository;
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseReviewHistoryRepository historyRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;

    Disease requireDisease(UUID id) {
        return diseaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISEASE_NOT_FOUND));
    }

    void requireActiveCropType(UUID cropTypeId) {
        var cropType = cropTypeRepository.findById(cropTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
        if (!cropType.isActive()) {
            throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
        }
    }

    Disease requireAccessibleDisease(UUID id) {
        Disease disease = requireDisease(id);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(disease.getBrandId())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return disease;
    }

    Disease requireBrandOwnedDisease(UUID id) {
        Disease disease = requireDisease(id);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (!currentUser.hasRole(RoleConstant.ROLE_BRAND)
                || !currentUser.getUserId().equals(disease.getBrandId())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return disease;
    }

    void requireBrandEditable(Disease disease) {
        ReviewStatus status = disease.getReviewStatus();
        if (status != ReviewStatus.DRAFT && status != ReviewStatus.REJECTED) {
            throw new BusinessException(ErrorCode.DISEASE_STATUS_TRANSITION_INVALID);
        }
    }

    void requireStatus(Disease disease, ReviewStatus expectedStatus) {
        if (disease.getReviewStatus() != expectedStatus) {
            throw new BusinessException(ErrorCode.DISEASE_STATUS_TRANSITION_INVALID);
        }
    }

    void recordHistory(
            Disease disease,
            DiseaseReviewAction action,
            ReviewStatus previousStatus,
            String comment,
            UUID actorId,
            ReviewActorType actorType,
            Instant now
    ) {
        historyRepository.save(DiseaseReviewHistory.create(
                idGenerator.generate(),
                disease.getId(),
                action,
                previousStatus,
                disease.getReviewStatus(),
                comment,
                actorId,
                actorType,
                now
        ));
    }

    ReviewActorType actorType(CurrentUser currentUser) {
        return currentUser.hasRole(RoleConstant.ROLE_ADMIN)
                ? ReviewActorType.ADMIN
                : ReviewActorType.BRAND;
    }
}
