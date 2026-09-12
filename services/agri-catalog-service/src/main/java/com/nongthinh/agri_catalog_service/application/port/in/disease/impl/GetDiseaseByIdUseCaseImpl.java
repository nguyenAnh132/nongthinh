package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseByIdUseCase;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDiseaseByIdUseCaseImpl implements GetDiseaseByIdUseCase {

    private final DiseaseUseCaseSupport support;

    @Override
    public DiseaseView execute(UUID id, boolean publicOnly) {
        Disease disease = publicOnly
                ? support.requireDisease(id)
                : support.requireAccessibleDisease(id);
        if (publicOnly && disease.getReviewStatus() != ReviewStatus.APPROVED) {
            throw new BusinessException(ErrorCode.DISEASE_NOT_FOUND);
        }
        return DiseaseView.from(disease);
    }
}
