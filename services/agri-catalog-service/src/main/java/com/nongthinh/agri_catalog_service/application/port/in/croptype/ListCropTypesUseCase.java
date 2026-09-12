package com.nongthinh.agri_catalog_service.application.port.in.croptype;

import java.util.List;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;

public interface ListCropTypesUseCase {

    List<CropTypeView> execute(boolean activeOnly);
}

