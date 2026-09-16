package com.nongthinh.agri_catalog_service.application.view;

import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.Product;

public record ProductSummaryView(
        UUID id,
        UUID brandId,
        String name,
        String slug,
        String shortDescription,
        String manufacturerName,
        String thumbnailUrl,
        String purchaseUrl
) {
    public static ProductSummaryView from(Product product) {
        return new ProductSummaryView(
                product.getId(),
                product.getBrandId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getManufacturerName(),
                product.getThumbnailUrl(),
                product.getPurchaseUrl());
    }
}
