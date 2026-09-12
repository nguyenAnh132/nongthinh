package com.nongthinh.location_service.application.view;

import java.util.UUID;

public record AddressView(
        String provinceId,
        String provinceName,
        UUID communeId,
        String communeName
) {
}
