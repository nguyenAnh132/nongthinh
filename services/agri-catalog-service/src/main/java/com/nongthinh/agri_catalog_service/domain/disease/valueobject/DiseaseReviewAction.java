package com.nongthinh.agri_catalog_service.domain.disease.valueobject;

public enum DiseaseReviewAction {

    CREATED,
    SUBMITTED,
    APPROVED,
    REJECTED,
    HIDDEN,
    RESTORED;

    public static DiseaseReviewAction fromString(String value) {
        return switch (value) {
            case "CREATED" -> CREATED;
            case "SUBMITTED" -> SUBMITTED;
            case "APPROVED" -> APPROVED;
            case "REJECTED" -> REJECTED;
            case "HIDDEN" -> HIDDEN;
            case "RESTORED" -> RESTORED;
            default -> throw new IllegalArgumentException("Invalid disease review action: " + value);
        };
    }

    public String getValue() {
        return name();
    }

}
