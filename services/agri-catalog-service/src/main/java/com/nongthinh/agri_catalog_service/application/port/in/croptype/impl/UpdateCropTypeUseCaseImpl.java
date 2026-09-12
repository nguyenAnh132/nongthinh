package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.CropTypeUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.UpdateCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCropTypeUseCaseImpl implements UpdateCropTypeUseCase {

    private final CropTypeUseCaseSupport support;
    private final CropTypeRepository cropTypeRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public CropTypeView execute(UUID id, CropTypeUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");

        var cropType = support.requireCropType(id);
        cropType.update(
                command.name(),
                command.description(),
                command.active(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        return CropTypeView.from(cropTypeRepository.save(cropType));
    }
}

