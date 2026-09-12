package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentUpdateCommand;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;

public interface UpdateProductDiseaseTreatmentUseCase {

    ProductDiseaseTreatmentView execute(
            UUID productId,
            UUID treatmentId,
            ProductDiseaseTreatmentUpdateCommand command
    );
}
