package com.nongthinh.profile_service.application.view;

public record AddressNamesView(
        String provinceName,
        String communeName
) {
    public static AddressNamesView empty() {
        return new AddressNamesView(null, null);
    }
}
