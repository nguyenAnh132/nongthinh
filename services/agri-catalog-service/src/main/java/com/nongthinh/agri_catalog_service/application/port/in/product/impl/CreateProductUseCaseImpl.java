package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.product.CreateProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ProductHistoryAction;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductUseCaseSupport support;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductView execute(ProductCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        support.requireBrandIdentity(command.brandId(), currentUser);
        validateCategoryExists(command.categoryId());
        if (productRepository.existsByBrandIdAndSlug(command.brandId(), command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }

        UUID actorId = currentUser.getUserId();
        Instant now = clockProvider.now();
        Product product = Product.create(
                idGenerator.generate(),
                command.brandId(),
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
                null,
                command.purchaseUrl(),
                PublicationStatus.DRAFT,
                ModerationStatus.NORMAL,
                null,
                actorId,
                now
        );
        Product saved = productRepository.save(product);
        support.recordHistory(
                saved,
                ProductHistoryAction.CREATED,
                null,
                null,
                null,
                "Product created",
                currentUser,
                now
        );
        return ProductView.from(saved);
    }

    private void validateCategoryExists(UUID categoryId) {
        if (productCategoryRepository.findById(categoryId).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
    }
}
