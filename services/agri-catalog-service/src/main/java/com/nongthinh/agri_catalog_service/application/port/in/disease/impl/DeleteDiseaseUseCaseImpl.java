package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.disease.DeleteDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteDiseaseUseCaseImpl implements DeleteDiseaseUseCase {

    private final DiseaseUseCaseSupport support;
    private final DiseaseRepository diseaseRepository;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        Disease disease = support.requireAccessibleDisease(id);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)) {
            support.requireBrandEditable(disease);
        }

        UUID actorId = currentUser.getUserId();
        Instant now = clockProvider.now();
        for (ProductDiseaseTreatment treatment
                : treatmentRepository.findAllByDiseaseIdOrderByPriority(id)) {
            treatment.delete(actorId, now);
            treatmentRepository.save(treatment);
        }
        disease.delete(actorId, now);
        diseaseRepository.save(disease);
    }
}
