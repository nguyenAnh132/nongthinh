package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.ListProductDiseaseTreatmentsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductDiseaseTreatmentsUseCaseImpl
        implements ListProductDiseaseTreatmentsUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;
    private final ProductDiseaseTreatmentRepository treatmentRepository;

    @Override
    public List<ProductDiseaseTreatmentView> execute(UUID productId, boolean publicOnly) {
        List<ProductDiseaseTreatment> treatments;
        if (publicOnly) {
            support.requirePublicProduct(productId);
            treatments = treatmentRepository
                    .findAllPublicByProductIdOrderByPriority(productId);
        } else {
            support.requireWritableProduct(productId);
            treatments = treatmentRepository.findAllByProductIdOrderByPriority(productId);
        }
        return treatments.stream()
                .map(ProductDiseaseTreatmentView::from)
                .toList();
    }
}
