package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum ProductReviewStatus {

    VISIBLE,
    HIDDEN;

    public static ProductReviewStatus fromString(String value) {
        return switch (value) {
            case "VISIBLE" -> VISIBLE;
            case "HIDDEN" -> HIDDEN;
            default -> throw new IllegalArgumentException("Invalid product review status: " + value);
        };
    }

    public String getValue() {
        return name();
    }

}
