package com.nongthinh.agri_catalog_service.application.port.in.disease.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.disease.ListDiseaseReviewHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseReviewHistoryRepository;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListDiseaseReviewHistoryUseCaseImpl implements ListDiseaseReviewHistoryUseCase {

    private final DiseaseUseCaseSupport support;
    private final DiseaseReviewHistoryRepository historyRepository;

    @Override
    public List<DiseaseReviewHistoryView> execute(UUID diseaseId) {
        support.requireAccessibleDisease(diseaseId);
        return historyRepository.findAllByDiseaseIdOrderByCreatedAtDesc(diseaseId)
                .stream()
                .map(DiseaseReviewHistoryView::from)
                .toList();
    }
}
