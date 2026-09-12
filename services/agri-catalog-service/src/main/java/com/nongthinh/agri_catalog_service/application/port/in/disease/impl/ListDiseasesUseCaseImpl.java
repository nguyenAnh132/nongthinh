package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseasesUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListDiseasesUseCaseImpl implements ListDiseasesUseCase {

    private final DiseaseRepository diseaseRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<DiseaseView> execute(
            UUID brandId,
            ReviewStatus reviewStatus,
            UUID cropTypeId,
            boolean publicOnly
    ) {
        List<Disease> diseases;
        if (publicOnly) {
            diseases = diseaseRepository.findAllApproved(cropTypeId);
        } else {
            CurrentUser currentUser = currentUserProvider.getCurrentUser();
            UUID effectiveBrandId = currentUser.hasRole(RoleConstant.ROLE_BRAND)
                    ? currentUser.getUserId()
                    : brandId;
            diseases = diseaseRepository.findAll(effectiveBrandId, reviewStatus, cropTypeId);
        }
        return diseases.stream()
                .map(DiseaseView::from)
                .toList();
    }
}
