package com.nongthinh.agri_catalog_service.domain.disease.valueobject;

public enum CreatedSource {

    ADMIN,
    BRAND;

    public static CreatedSource fromString(String value) {
        return switch (value) {
            case "ADMIN" -> ADMIN;
            case "BRAND" -> BRAND;
            default -> throw new IllegalArgumentException("Invalid created source: " + value);
        };
    }

    public String getValue() {
        return name();
    }

}
