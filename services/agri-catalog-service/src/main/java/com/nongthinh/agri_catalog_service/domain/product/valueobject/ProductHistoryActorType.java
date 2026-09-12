package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum ProductHistoryActorType {
    BRAND,
    ADMIN,
    SYSTEM;

    public static ProductHistoryActorType fromString(String value) {
        return switch (value) {
            case "BRAND" -> BRAND;
            case "ADMIN" -> ADMIN;
            case "SYSTEM" -> SYSTEM;
            default -> throw new IllegalArgumentException("Invalid product history actor type: " + value);
        };
    }

    public String getValue() {
        return name();
    }
}
