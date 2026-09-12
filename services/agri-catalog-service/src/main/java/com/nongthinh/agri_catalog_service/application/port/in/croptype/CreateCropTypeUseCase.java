package com.nongthinh.agri_catalog_service.application.port.in.croptype;

import com.nongthinh.agri_catalog_service.application.command.CropTypeCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;

public interface CreateCropTypeUseCase {

    CropTypeView execute(CropTypeCreationCommand command);
}

