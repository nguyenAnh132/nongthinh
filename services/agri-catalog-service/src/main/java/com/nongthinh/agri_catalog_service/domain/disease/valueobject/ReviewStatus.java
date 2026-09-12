package com.nongthinh.agri_catalog_service.domain.disease.valueobject;

public enum ReviewStatus {

    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    HIDDEN,
    REJECTED;

    public static ReviewStatus fromString(String value) {
        return switch (value) {
            case "DRAFT" -> DRAFT;
            case "PENDING_REVIEW" -> PENDING_REVIEW;
            case "APPROVED" -> APPROVED;
            case "HIDDEN" -> HIDDEN;
            case "REJECTED" -> REJECTED;
            default -> throw new IllegalArgumentException("Invalid review status: " + value);
        };
    }

    public String getValue() {
        return name();
    }
}
