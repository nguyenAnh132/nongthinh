package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.UpdateProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductDiseaseTreatmentUseCaseImpl
        implements UpdateProductDiseaseTreatmentUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductDiseaseTreatmentView execute(
            UUID productId,
            UUID treatmentId,
            ProductDiseaseTreatmentUpdateCommand command
    ) {
        Objects.requireNonNull(command, "command is required");
        support.requireWritableProduct(productId);
        ProductDiseaseTreatment treatment = support.requireTreatment(
                productId,
                treatmentId
        );
        treatment.update(
                command.effectivenessLevel(),
                command.priority(),
                command.dosage(),
                command.applicationMethod(),
                command.applicationTiming(),
                command.frequencyInstruction(),
                command.treatmentNote(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        return ProductDiseaseTreatmentView.from(treatmentRepository.save(treatment));
    }
}
