package com.nongthinh.agri_catalog_service.application.port.in.category.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.category.ListProductCategoriesUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductCategoryView;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductCategoriesUseCaseImpl implements ListProductCategoriesUseCase {

    private final ProductCategoryRepository productCategoryRepository;

    @Override
    public List<ProductCategoryView> execute(boolean activeOnly) {
        List<ProductCategory> productCategories = activeOnly
                ? productCategoryRepository.findAllActiveOrderByDisplayOrder()
                : productCategoryRepository.findAllOrderByDisplayOrder();
        return productCategories.stream()
                .map(ProductCategoryView::from)
                .toList();
    }
}
