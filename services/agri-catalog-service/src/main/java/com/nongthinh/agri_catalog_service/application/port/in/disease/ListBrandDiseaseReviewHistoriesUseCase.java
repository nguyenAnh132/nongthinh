package com.nongthinh.agri_catalog_service.application.port.in.disease;

import com.nongthinh.agri_catalog_service.application.query.DiseaseReviewHistoryQuery;
import com.nongthinh.agri_catalog_service.application.view.DiseaseReviewHistoryListItemView;
import com.nongthinh.agri_catalog_service.application.view.PageView;

public interface ListBrandDiseaseReviewHistoriesUseCase {

    PageView<DiseaseReviewHistoryListItemView> execute(DiseaseReviewHistoryQuery query);
}
