package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentCreationCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;

public interface CreateProductDiseaseTreatmentUseCase {

    ProductDiseaseTreatmentView execute(
            UUID productId,
            ProductDiseaseTreatmentCreationCommand command
    );
}
