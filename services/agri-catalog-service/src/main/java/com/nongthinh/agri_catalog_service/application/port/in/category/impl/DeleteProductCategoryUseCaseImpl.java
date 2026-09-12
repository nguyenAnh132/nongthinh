package com.nongthinh.agri_catalog_service.application.port.in.category.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.category.DeleteProductCategoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProductCategoryUseCaseImpl implements DeleteProductCategoryUseCase {

    private final ProductCategoryRepository productCategoryRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        ProductCategory productCategory = productCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));

        if (!productCategoryRepository.findByParentIdOrderByDisplayOrder(id).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_CATEGORY_HAS_CHILDREN);
        }

        productCategory.delete(
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        productCategoryRepository.save(productCategory);
    }
}
