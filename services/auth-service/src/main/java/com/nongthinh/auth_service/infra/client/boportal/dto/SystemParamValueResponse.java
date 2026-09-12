package com.nongthinh.auth_service.infra.client.boportal.dto;

public record SystemParamValueResponse(
        String name,
        String value,
        String dataType
) {
}
