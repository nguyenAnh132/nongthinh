package com.nongthinh.post_service.infra.client.agricatalog;

public record AgriCatalogServiceResponse<T>(String code, String message, T result) {
}
