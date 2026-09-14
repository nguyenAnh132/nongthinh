package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.model.ProductRatingSummary;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseaseRecommendationsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ProductRatingQuery;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseRecommendationView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListDiseaseRecommendationsUseCaseImpl implements ListDiseaseRecommendationsUseCase {
    private static final int PAGE_SIZE = 10;

    private final DiseaseRepository diseaseRepository;
    private final ProductDiseaseTreatmentRepository treatmentRepository;
    private final ProductRatingQuery productRatingQuery;

    @Override
    @Transactional(readOnly = true)
    public PageView<DiseaseRecommendationView> execute(UUID diseaseId, int page) {
        if (diseaseId == null || page < 0 || page > Integer.MAX_VALUE / PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        var disease = diseaseRepository.findById(diseaseId)
                .filter(item -> !item.isDeleted() && item.getReviewStatus() == ReviewStatus.APPROVED)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISEASE_NOT_FOUND));
        var result = treatmentRepository.findRankedPublicRecommendationsByDiseaseId(
                disease.getId(), page, PAGE_SIZE);
        var ratings = productRatingQuery.summarizeVisibleReviews(
                result.items().stream().map(item -> item.product().getId()).toList());
        var items = result.items().stream()
                .map(item -> DiseaseRecommendationView.from(item, ratings.getOrDefault(
                        item.product().getId(), ProductRatingSummary.unrated(item.product().getId()))))
                .toList();
        return new PageView<>(items, result.page(), result.size(), result.totalElements(),
                result.totalPages(), result.hasNext());
    }
}
