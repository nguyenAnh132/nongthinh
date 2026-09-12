package com.nongthinh.brand_service.domain.brandapprovalprocess;

public enum BrandApprovalProcessStatus {

    STARTED("STARTED"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String value;

    BrandApprovalProcessStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BrandApprovalProcessStatus fromString(String value) {
        for (BrandApprovalProcessStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BrandApprovalProcessStatus: " + value);
    }
}
