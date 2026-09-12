package com.nongthinh.location_service.application.view;

import com.nongthinh.location_service.domain.province.Province;

public record ProvinceView(
        String id,
        String code,
        String name
) {
    public static ProvinceView from(Province province) {
        return new ProvinceView(province.getId(), province.getCode(), province.getName());
    }
}
