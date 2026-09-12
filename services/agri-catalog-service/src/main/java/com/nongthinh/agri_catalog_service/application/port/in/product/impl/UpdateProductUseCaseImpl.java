package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.product.UpdateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
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
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private final ProductUseCaseSupport support;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductView execute(UUID id, ProductUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product product = support.requireAccessibleProduct(id);
        if (productCategoryRepository.findById(command.categoryId()).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
        if (!product.getSlug().equalsIgnoreCase(command.slug())
                && productRepository.existsByBrandIdAndSlug(product.getBrandId(), command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        PublicationStatus previousPublicationStatus = product.getPublicationStatus();
        ModerationStatus previousModerationStatus = product.getModerationStatus();
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        var now = clockProvider.now();
        product.update(
                command.categoryId(),
                command.name(),
                command.slug(),
                command.sku(),
                command.registrationNumber(),
                command.manufacturerName(),
                command.originCountry(),
                command.shortDescription(),
                command.description(),
                command.ingredients(),
                command.usageInstruction(),
                command.dosageInstruction(),
                command.safetyInstruction(),
                command.storageInstruction(),
                command.warning(),
                command.form(),
                command.unit(),
                command.packageSpecification(),
                command.purchaseUrl(),
                currentUser.getUserId(),
                now
        );
        Product saved = productRepository.save(product);
        support.recordHistory(
                saved,
                ProductHistoryAction.UPDATED,
                previousPublicationStatus,
                previousModerationStatus,
                null,
                "Product information updated",
                currentUser,
                now
        );
        return ProductView.from(saved);
    }
}
