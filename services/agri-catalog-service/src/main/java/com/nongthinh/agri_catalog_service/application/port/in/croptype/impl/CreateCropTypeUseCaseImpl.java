package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.CropTypeCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.CreateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCropTypeUseCaseImpl implements CreateCropTypeUseCase {

    private final CropTypeRepository cropTypeRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CropTypeView execute(CropTypeCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (cropTypeRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.CROP_TYPE_CODE_ALREADY_EXISTS);
        }

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        CropType cropType = CropType.create(
                idGenerator.generate(),
                command.code(),
                command.name(),
                command.description(),
                actorId,
                now
        );
        return CropTypeView.from(cropTypeRepository.save(cropType));
    }
}

