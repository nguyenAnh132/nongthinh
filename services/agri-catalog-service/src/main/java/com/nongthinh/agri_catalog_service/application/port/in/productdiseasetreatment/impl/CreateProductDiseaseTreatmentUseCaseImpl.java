package com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.productdiseasetreatment.CreateProductDiseaseTreatmentUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductDiseaseTreatmentView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductDiseaseTreatmentUseCaseImpl
        implements CreateProductDiseaseTreatmentUseCase {

    private final ProductDiseaseTreatmentUseCaseSupport support;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductDiseaseTreatmentView execute(
            UUID productId,
            ProductDiseaseTreatmentCreationCommand command
    ) {
        Objects.requireNonNull(command, "command is required");
        Product product = support.requireWritableProduct(productId);
        support.requireApprovedDisease(command.diseaseId());
        if (treatmentRepository.existsByProductIdAndDiseaseId(
                productId,
                command.diseaseId()
        )) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_DISEASE_TREATMENT_ALREADY_EXISTS
            );
        }

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        ProductDiseaseTreatment treatment = ProductDiseaseTreatment.create(
                idGenerator.generate(),
                productId,
                command.diseaseId(),
                product.getBrandId(),
                command.effectivenessLevel(),
                command.priority(),
                command.dosage(),
                command.applicationMethod(),
                command.applicationTiming(),
                command.frequencyInstruction(),
                command.treatmentNote(),
                actorId,
                now
        );
        return ProductDiseaseTreatmentView.from(treatmentRepository.save(treatment));
    }
}
