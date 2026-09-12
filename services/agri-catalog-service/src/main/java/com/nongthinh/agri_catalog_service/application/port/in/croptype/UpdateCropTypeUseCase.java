package com.nongthinh.agri_catalog_service.application.port.in.croptype;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.CropTypeUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;

public interface UpdateCropTypeUseCase {

    CropTypeView execute(UUID id, CropTypeUpdateCommand command);
}

