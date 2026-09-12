package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.SnapshotSerializer;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductHistoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductHistory;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryActorType;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ProductUseCaseSupport {

    private final ProductRepository productRepository;
    private final ProductHistoryRepository historyRepository;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final SnapshotSerializer snapshotSerializer;

    Product requireAccessibleProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(product.getBrandId())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return product;
    }

    void requireBrandIdentity(UUID brandId, CurrentUser currentUser) {
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(brandId)) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
    }

    ProductHistoryActorType actorType(CurrentUser currentUser) {
        return currentUser.hasRole(RoleConstant.ROLE_ADMIN)
                ? ProductHistoryActorType.ADMIN
                : ProductHistoryActorType.BRAND;
    }

    void recordHistory(
            Product product,
            ProductHistoryAction action,
            PublicationStatus previousPublicationStatus,
            ModerationStatus previousModerationStatus,
            String reason,
            String changeSummary,
            CurrentUser actor,
            Instant now
    ) {
        historyRepository.save(ProductHistory.create(
                idGenerator.generate(),
                product.getId(),
                action,
                actor.getUserId(),
                actorType(actor),
                previousPublicationStatus,
                product.getPublicationStatus(),
                previousModerationStatus,
                product.getModerationStatus(),
                reason,
                changeSummary,
                snapshotSerializer.serialize(ProductView.from(product)),
                now
        ));
    }
}
