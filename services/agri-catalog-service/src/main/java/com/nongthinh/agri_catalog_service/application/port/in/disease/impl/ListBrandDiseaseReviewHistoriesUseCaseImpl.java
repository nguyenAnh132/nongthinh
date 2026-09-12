package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListBrandDiseaseReviewHistoriesUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.CreatedSource;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListBrandDiseaseReviewHistoriesUseCaseImpl
        implements ListBrandDiseaseReviewHistoriesUseCase {

    private final DiseaseReviewHistoryRepository historyRepository;

    @Override
    @Transactional(readOnly = true)
    public PageView<DiseaseReviewHistoryListItemView> execute(
            DiseaseReviewHistoryQuery query
    ) {
        validate(query);
        DiseaseReviewHistoryQuery normalized = new DiseaseReviewHistoryQuery(
                CreatedSource.BRAND,
                query.diseaseId(),
                query.brandId(),
                query.action(),
                query.newStatus(),
                query.actorType(),
                query.actorId(),
                normalizeKeyword(query.keyword()),
                query.from(),
                query.to(),
                query.page(),
                query.size()
        );
        return historyRepository.search(normalized);
    }

    private static void validate(DiseaseReviewHistoryQuery query) {
        if (query == null
                || (query.createdSource() != null
                    && query.createdSource() != CreatedSource.BRAND)
                || query.page() < 0
                || query.size() < 1
                || query.size() > 100
                || (long) query.page() * query.size() > Integer.MAX_VALUE
                || (query.from() != null
                    && query.to() != null
                    && query.from().isAfter(query.to()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
