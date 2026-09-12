package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.DiseaseCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.disease.CreateDiseaseUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseView;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.DiseaseReviewAction;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateDiseaseUseCaseImpl implements CreateDiseaseUseCase {

    private final DiseaseUseCaseSupport support;
    private final DiseaseRepository diseaseRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DiseaseView execute(DiseaseCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        support.requireActiveCropType(command.cropTypeId());
        if (diseaseRepository.existsBySlugAndCropTypeId(command.slug(), command.cropTypeId())) {
            throw new BusinessException(ErrorCode.DISEASE_SLUG_ALREADY_EXISTS);
        }

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID actorId = currentUser.getUserId();
        Instant now = clockProvider.now();
        CreatedSource source = currentUser.hasRole(RoleConstant.ROLE_ADMIN)
                ? CreatedSource.ADMIN
                : CreatedSource.BRAND;
        UUID brandId = source == CreatedSource.BRAND ? actorId : null;

        Disease disease = Disease.create(
                idGenerator.generate(),
                source,
                brandId,
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
                actorId,
                now
        );
        Disease saved = diseaseRepository.save(disease);
        support.recordHistory(
                saved,
                DiseaseReviewAction.CREATED,
                null,
                null,
                actorId,
                support.actorType(currentUser),
                now
        );
        return DiseaseView.from(saved);
    }
}
