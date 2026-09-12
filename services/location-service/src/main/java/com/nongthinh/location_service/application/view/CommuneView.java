package com.nongthinh.location_service.application.view;

import java.util.UUID;
import com.nongthinh.location_service.domain.commune.Commune;

public record CommuneView(
        UUID id,
        String provinceId,
        String code,
        String name
) {
    public static CommuneView from(Commune commune) {
        return new CommuneView(
                commune.getId(),
                commune.getProvinceId(),
                commune.getCode(),
                commune.getName()
        );
    }
}
