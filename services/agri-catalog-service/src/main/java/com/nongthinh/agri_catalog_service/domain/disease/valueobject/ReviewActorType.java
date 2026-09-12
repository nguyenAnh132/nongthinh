package com.nongthinh.agri_catalog_service.domain.disease.valueobject;

public enum ReviewActorType {

    BRAND,
    ADMIN,
    SYSTEM;

    public static ReviewActorType fromString(String value) {
        return switch (value) {
            case "BRAND" -> BRAND;
            case "ADMIN" -> ADMIN;
            case "SYSTEM" -> SYSTEM;
            default -> throw new IllegalArgumentException("Invalid review actor type: " + value);
        };
    }
    
    public String getValue() {
        return name();
    }

}
