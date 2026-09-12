package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.GetCropTypeByIdUseCase;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCropTypeByIdUseCaseImpl implements GetCropTypeByIdUseCase {

    private final CropTypeUseCaseSupport support;

    @Override
    public CropTypeView execute(UUID id) {
        return CropTypeView.from(support.requireCropType(id));
    }
}

