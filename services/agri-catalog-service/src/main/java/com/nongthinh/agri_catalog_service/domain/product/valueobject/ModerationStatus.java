package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum ModerationStatus {

    NORMAL,
    LOCKED;

    public static ModerationStatus fromString(String value) {
        return ModerationStatus.valueOf(value.toUpperCase());
    }

    public String toString() {
        return name().toLowerCase();
    }
}
