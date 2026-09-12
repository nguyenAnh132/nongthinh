package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.domain.disease.DiseaseReviewHistory;

public interface DiseaseReviewHistoryRepository {

    DiseaseReviewHistory save(DiseaseReviewHistory diseaseReviewHistory);

    List<DiseaseReviewHistory> findAllByDiseaseIdOrderByCreatedAtDesc(UUID diseaseId);

    PageView<DiseaseReviewHistoryListItemView> search(DiseaseReviewHistoryQuery query);
}
