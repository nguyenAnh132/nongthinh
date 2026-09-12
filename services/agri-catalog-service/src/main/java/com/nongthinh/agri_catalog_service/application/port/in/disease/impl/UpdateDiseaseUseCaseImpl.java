package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.DiseaseUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.disease.UpdateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateDiseaseUseCaseImpl implements UpdateDiseaseUseCase {

    private final DiseaseUseCaseSupport support;
    private final DiseaseRepository diseaseRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DiseaseView execute(UUID id, DiseaseUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");
        Disease disease = support.requireAccessibleDisease(id);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)) {
            support.requireBrandEditable(disease);
        }

        support.requireActiveCropType(command.cropTypeId());
        boolean uniqueKeyChanged = !disease.getSlug().equalsIgnoreCase(command.slug())
                || !disease.getCropTypeId().equals(command.cropTypeId());
        if (uniqueKeyChanged
                && diseaseRepository.existsBySlugAndCropTypeId(
                        command.slug(),
                        command.cropTypeId()
                )) {
            throw new BusinessException(ErrorCode.DISEASE_SLUG_ALREADY_EXISTS);
        }

        disease.update(
                command.name(),
                command.slug(),
                command.scientificName(),
                command.cropTypeId(),
                command.affectedPart(),
                command.pathogenType(),
                command.shortDescription(),
                command.description(),
                command.symptoms(),
                command.causes(),
                command.favorableConditions(),
                command.preventionMethod(),
                command.treatmentGuideline(),
                command.thumbnailUrl(),
                currentUser.getUserId(),
                clockProvider.now()
        );
        return DiseaseView.from(diseaseRepository.save(disease));
    }
}
