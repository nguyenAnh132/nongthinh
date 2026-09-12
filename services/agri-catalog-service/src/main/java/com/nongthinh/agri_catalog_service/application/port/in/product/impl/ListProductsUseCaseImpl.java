package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductsUseCase;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductsUseCaseImpl implements ListProductsUseCase {

    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ProductRatingQuery productRatingQuery;

    @Override
    public List<ProductView> execute(UUID brandId, UUID categoryId, boolean publishedOnly) {
        List<Product> products;
        UUID effectiveBrandId = brandId;
        if (publishedOnly) {
            products = productRepository.findAllPublishedOrderByPublishedAtDesc();
        } else {
            CurrentUser currentUser = currentUserProvider.getCurrentUser();
            effectiveBrandId = currentUser.hasRole(RoleConstant.ROLE_BRAND)
                    ? currentUser.getUserId()
                    : brandId;
            if (effectiveBrandId != null) {
                products = productRepository.findAllByBrandId(effectiveBrandId);
            } else if (categoryId != null) {
                products = productRepository.findAllByCategoryId(categoryId);
            } else {
                products = productRepository.findAll();
            }
        }
        UUID resultBrandId = effectiveBrandId;
        List<Product> filteredProducts = products.stream()
                .filter(product -> categoryId == null || categoryId.equals(product.getCategoryId()))
                .filter(product -> resultBrandId == null
                        || resultBrandId.equals(product.getBrandId()))
                .toList();
        Map<UUID, ProductRatingSummary> ratings = productRatingQuery.summarizeVisibleReviews(
                filteredProducts.stream().map(Product::getId).toList()
        );
        return filteredProducts.stream()
                .map(product -> ProductView.from(
                        product,
                        ratings.getOrDefault(product.getId(), ProductRatingSummary.unrated(product.getId()))
                ))
                .toList();
    }
}
