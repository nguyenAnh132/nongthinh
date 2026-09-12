package com.nongthinh.agri_catalog_service.application.port.in.category.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.category.GetProductCategoryByIdUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProductCategoryByIdUseCaseImpl implements GetProductCategoryByIdUseCase {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public ProductCategoryView execute(UUID id) {
        return productCategoryRepository.findById(id)
                .map(ProductCategoryView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_CATEGORY_NOT_FOUND));
    }
}
