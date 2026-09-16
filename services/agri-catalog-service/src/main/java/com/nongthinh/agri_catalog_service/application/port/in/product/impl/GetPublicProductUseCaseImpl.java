package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.in.product.GetPublicProductUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.port.out.PublicProductTreatmentQuery;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductView;
import com.nongthinh.agri_catalog_service.application.view.PublicProductDetailView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicProductUseCaseImpl implements GetPublicProductUseCase {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRatingQuery ratingQuery;
    private final PublicProductTreatmentQuery treatmentQuery;

    @Override
    @Transactional(readOnly = true)
    public PublicProductDetailView execute(UUID productId) {
        var product = productRepository.findById(productId)
                .filter(item -> item.getDeletedAt() == null && item.getPublicationStatus() == PublicationStatus.PUBLISHED
                        && item.getModerationStatus() == ModerationStatus.NORMAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        var rating = ratingQuery.summarizeVisibleReviews(List.of(productId))
                .getOrDefault(productId, ProductRatingSummary.unrated(productId));
        String categoryName = categoryRepository.findById(product.getCategoryId()).map(item -> item.getName()).orElse(null);
        return new PublicProductDetailView(ProductView.from(product, rating), categoryName,
                treatmentQuery.findByProductId(productId));
    }
}
