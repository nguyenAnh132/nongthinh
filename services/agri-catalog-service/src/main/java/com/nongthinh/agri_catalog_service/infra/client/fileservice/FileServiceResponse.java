package com.nongthinh.agri_catalog_service.infra.client.fileservice;

public record FileServiceResponse<T>(
        String code,
        String message,
        T result
) {
}
