package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.DeleteProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProductDiseaseTreatmentUseCaseImpl
        implements DeleteProductDiseaseTreatmentUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID productId, UUID treatmentId) {
        support.requireWritableProduct(productId);
        ProductDiseaseTreatment treatment = support.requireTreatment(
                productId,
                treatmentId
        );
        treatment.delete(
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        treatmentRepository.save(treatment);
    }
}
