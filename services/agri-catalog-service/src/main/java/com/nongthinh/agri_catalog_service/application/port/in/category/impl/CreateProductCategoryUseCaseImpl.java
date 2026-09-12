package com.nongthinh.agri_catalog_service.application.port.in.category.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductCategoryCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.category.CreateProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductCategoryUseCaseImpl implements CreateProductCategoryUseCase {

    private final ProductCategoryRepository productCategoryRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductCategoryView execute(ProductCategoryCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (productCategoryRepository.existsBySlug(command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_SLUG_ALREADY_EXISTS);
        }
        validateParentExists(command.parentId());

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        ProductCategory productCategory = ProductCategory.create(
                idGenerator.generate(),
                command.parentId(),
                command.name(),
                command.slug(),
                command.description(),
                command.displayOrder(),
                actorId,
                actorId,
                null,
                now
        );
        return ProductCategoryView.from(productCategoryRepository.save(productCategory));
    }

    private void validateParentExists(UUID parentId) {
        if (parentId != null && productCategoryRepository.findById(parentId).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND);
        }
    }
}
