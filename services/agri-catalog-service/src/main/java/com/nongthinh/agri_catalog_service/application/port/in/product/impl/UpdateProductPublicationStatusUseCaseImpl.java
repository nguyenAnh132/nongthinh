package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductPublicationStatusUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductPublicationStatusUseCaseImpl implements UpdateProductPublicationStatusUseCase {

    private final ProductUseCaseSupport support;
    private final ProductRepository productRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductView execute(UUID id, PublicationStatus publicationStatus) {
        Product product = support.requireAccessibleProduct(id);
        if (product.getModerationStatus() == ModerationStatus.LOCKED) {
            throw new BusinessException(ErrorCode.PRODUCT_LOCKED);
        }

        PublicationStatus previousPublicationStatus = product.getPublicationStatus();
        ModerationStatus previousModerationStatus = product.getModerationStatus();
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        var now = clockProvider.now();
        ProductHistoryAction action;
        String changeSummary;

        if (publicationStatus == PublicationStatus.PUBLISHED) {
            product.publish(currentUser.getUserId(), now);
            action = ProductHistoryAction.PUBLISHED;
            changeSummary = "Product published";
        } else if (publicationStatus == PublicationStatus.UNPUBLISHED) {
            product.unpublish(currentUser.getUserId(), now);
            action = ProductHistoryAction.UNPUBLISHED;
            changeSummary = "Product unpublished";
        } else {
            throw new BusinessException(ErrorCode.PRODUCT_PUBLICATION_STATUS_INVALID);
        }

        Product saved = productRepository.save(product);
        support.recordHistory(
                saved,
                action,
                previousPublicationStatus,
                previousModerationStatus,
                null,
                changeSummary,
                currentUser,
                now
        );
        return ProductView.from(saved);
    }
}
