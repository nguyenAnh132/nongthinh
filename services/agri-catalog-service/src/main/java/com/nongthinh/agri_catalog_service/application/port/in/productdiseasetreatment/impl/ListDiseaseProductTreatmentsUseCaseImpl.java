package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.ListDiseaseProductTreatmentsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListDiseaseProductTreatmentsUseCaseImpl
        implements ListDiseaseProductTreatmentsUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;
    private final ProductDiseaseTreatmentRepository treatmentRepository;

    @Override
    public List<ProductDiseaseTreatmentView> execute(UUID diseaseId, boolean publicOnly) {
        List<ProductDiseaseTreatment> treatments;
        if (publicOnly) {
            support.requirePublicDisease(diseaseId);
            treatments = treatmentRepository
                    .findAllPublicByDiseaseIdOrderByPriority(diseaseId);
        } else {
            support.requireDisease(diseaseId);
            treatments = treatmentRepository
                    .findAllByDiseaseIdOrderByPriority(diseaseId);
        }
        return treatments.stream()
                .map(ProductDiseaseTreatmentView::from)
                .toList();
    }
}
