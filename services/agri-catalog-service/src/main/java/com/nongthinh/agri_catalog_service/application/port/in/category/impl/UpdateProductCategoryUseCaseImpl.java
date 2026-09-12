package com.nongthinh.agri_catalog_service.application.port.in.category.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductCategoryUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.category.UpdateProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductCategoryUseCaseImpl implements UpdateProductCategoryUseCase {

    private final ProductCategoryRepository productCategoryRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductCategoryView execute(UUID id, ProductCategoryUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");

        ProductCategory productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        if (!productCategory.getSlug().equalsIgnoreCase(command.slug())
                && productCategoryRepository.existsBySlug(command.slug())) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_SLUG_ALREADY_EXISTS);
        }
        validateParent(id, command.parentId());

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        productCategory.update(
                command.parentId(),
                command.name(),
                command.slug(),
                command.description(),
                command.displayOrder(),
                command.active(),
                actorId,
                clockProvider.now()
        );
        return ProductCategoryView.from(productCategoryRepository.save(productCategory));
    }

    private void validateParent(UUID categoryId, UUID parentId) {
        Set<UUID> visitedIds = new HashSet<>();
        UUID currentId = parentId;
        while (currentId != null) {
            if (categoryId.equals(currentId) || !visitedIds.add(currentId)) {
                throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_PARENT_INVALID);
            }
            ProductCategory parent = productCategoryRepository.findById(currentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
            currentId = parent.getParentId();
        }
    }
}
