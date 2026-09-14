package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.in.product.SearchPublicProductsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.query.PublicProductQuery;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchPublicProductsUseCaseImpl implements SearchPublicProductsUseCase {
    private final ProductRepository productRepository;
    private final ProductRatingQuery productRatingQuery;

    @Override
    @Transactional(readOnly = true)
    public PageView<ProductView> execute(PublicProductQuery query) {
        var page = productRepository.searchPublic(query);
        var ratings = productRatingQuery.summarizeVisibleReviews(page.items().stream().map(Product::getId).toList());
        var items = page.items().stream().map(product -> ProductView.from(product,
                ratings.getOrDefault(product.getId(), ProductRatingSummary.unrated(product.getId())))).toList();
        return new PageView<>(items, page.page(), page.size(), page.totalElements(), page.totalPages(), page.hasNext());
    }
}
