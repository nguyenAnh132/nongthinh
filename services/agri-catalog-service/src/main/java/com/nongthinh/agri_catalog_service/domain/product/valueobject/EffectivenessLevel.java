package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum EffectivenessLevel {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH;

    public static EffectivenessLevel fromString(String value) {
        return switch (value) {
            case "LOW" -> LOW;
            case "MEDIUM" -> MEDIUM;
            case "HIGH" -> HIGH;
            case "VERY_HIGH" -> VERY_HIGH;
            default -> throw new IllegalArgumentException("Invalid effectiveness level: " + value);
        };
    }

    public String getValue() {
        return name();
    }
}
