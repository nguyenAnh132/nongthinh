package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.product.DeleteProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProductUseCaseImpl implements DeleteProductUseCase {

    private final ProductUseCaseSupport support;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductDiseaseTreatmentRepository productDiseaseTreatmentRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        Product product = support.requireAccessibleProduct(id);
        PublicationStatus previousPublicationStatus = product.getPublicationStatus();
        ModerationStatus previousModerationStatus = product.getModerationStatus();
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID actorId = currentUser.getUserId();
        var now = clockProvider.now();
        product.delete(actorId, now);
        for (ProductImage image
                : productImageRepository.findAllByProductIdOrderByDisplayOrder(id)) {
            image.delete(actorId, now);
            productImageRepository.save(image);
        }
        for (ProductDiseaseTreatment treatment
                : productDiseaseTreatmentRepository.findAllByProductIdOrderByPriority(id)) {
            treatment.delete(actorId, now);
            productDiseaseTreatmentRepository.save(treatment);
        }
        Product saved = productRepository.save(product);
        support.recordHistory(
                saved,
                ProductHistoryAction.DELETED,
                previousPublicationStatus,
                previousModerationStatus,
                "Product soft deleted",
                "Product and related catalog data marked as deleted",
                currentUser,
                now
        );
    }
}
