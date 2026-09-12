package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum PublicationStatus {

    DRAFT,
    PUBLISHED,
    UNPUBLISHED;

    public static PublicationStatus fromString(String value) {
        return PublicationStatus.valueOf(value.toUpperCase());
    }

    public String toString() {
        return name().toLowerCase();
    }
}
