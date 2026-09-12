package com.nongthinh.agri_catalog_service.domain.product.valueobject;

public enum ProductHistoryAction {

    CREATED,
    UPDATED,
    PUBLISHED,
    UNPUBLISHED,
    LOCKED,
    UNLOCKED,
    DELETED,
    RESTORED;

    public static ProductHistoryAction fromString(String value) {
        return switch (value) {
            case "CREATED" -> CREATED;
            case "UPDATED" -> UPDATED;
            case "PUBLISHED" -> PUBLISHED;
            case "UNPUBLISHED" -> UNPUBLISHED;
            case "LOCKED" -> LOCKED;
            case "UNLOCKED" -> UNLOCKED;
            case "DELETED" -> DELETED;
            case "RESTORED" -> RESTORED;
            default -> throw new IllegalArgumentException("Invalid product history action: " + value);
        };
    }

    public String getValue() {
        return name();
    }

}
