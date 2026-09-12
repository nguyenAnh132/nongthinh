package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.GetProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProductDiseaseTreatmentUseCaseImpl
        implements GetProductDiseaseTreatmentUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;

    @Override
    public ProductDiseaseTreatmentView execute(UUID productId, UUID treatmentId) {
        support.requireWritableProduct(productId);
        return ProductDiseaseTreatmentView.from(
                support.requireTreatment(productId, treatmentId)
        );
    }
}
