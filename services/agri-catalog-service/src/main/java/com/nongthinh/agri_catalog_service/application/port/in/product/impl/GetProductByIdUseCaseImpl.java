package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.product.GetProductByIdUseCase;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCaseImpl implements GetProductByIdUseCase {

    private final ProductUseCaseSupport support;
    private final ProductRatingQuery productRatingQuery;

    @Override
    public ProductView execute(UUID id) {
        Product product = support.requireAccessibleProduct(id);
        ProductRatingSummary rating = productRatingQuery.summarizeVisibleReviews(List.of(id))
                .getOrDefault(id, ProductRatingSummary.unrated(id));
        return ProductView.from(product, rating);
    }
}
