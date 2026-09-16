package com.nongthinh.agri_catalog_service.application.view;

import java.util.List;

public record PublicProductDetailView(ProductView product, String categoryName,
        List<PublicProductTreatmentView> treatments) {
    public PublicProductDetailView {
        treatments = List.copyOf(treatments);
    }
}
