package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.disease.GetDiseaseRecommendationsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductDiseaseTreatmentRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseRecommendationView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDiseaseRecommendationsUseCaseImpl implements GetDiseaseRecommendationsUseCase {

    private static final int MAX_RECOMMENDATIONS = 3;

    private final DiseaseRepository diseaseRepository;
    private final ProductDiseaseTreatmentRepository treatmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DiseaseRecommendationView> execute(UUID diseaseId, int limit) {
        var disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISEASE_NOT_FOUND));
        if (disease.getReviewStatus() != ReviewStatus.APPROVED) {
            throw new BusinessException(ErrorCode.DISEASE_NOT_FOUND);
        }
        int boundedLimit = Math.min(Math.max(limit, 1), MAX_RECOMMENDATIONS);
        return treatmentRepository.findPublicRecommendationsByDiseaseId(diseaseId, boundedLimit).stream()
                .map(DiseaseRecommendationView::from)
                .toList();
    }
}
