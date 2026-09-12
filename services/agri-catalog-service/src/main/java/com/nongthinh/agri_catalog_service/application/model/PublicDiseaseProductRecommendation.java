package com.nongthinh.agri_catalog_service.application.model;

import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductDiseaseTreatment;

public record PublicDiseaseProductRecommendation(
        Product product,
        ProductDiseaseTreatment treatment
) {
}
