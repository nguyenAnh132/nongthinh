package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.disease.RejectDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewActorType;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RejectDiseaseUseCaseImpl implements RejectDiseaseUseCase {

    private final DiseaseUseCaseSupport support;
    private final DiseaseRepository diseaseRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DiseaseView execute(UUID id, String reason) {
        Disease disease = support.requireDisease(id);
        support.requireStatus(disease, ReviewStatus.PENDING_REVIEW);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        Instant now = clockProvider.now();

        disease.reject(reason, currentUser.getUserId(), now);
        Disease saved = diseaseRepository.save(disease);
        support.recordHistory(
                saved,
                DiseaseReviewAction.REJECTED,
                ReviewStatus.PENDING_REVIEW,
                reason,
                currentUser.getUserId(),
                ReviewActorType.ADMIN,
                now
        );
        return DiseaseView.from(saved);
    }
}
