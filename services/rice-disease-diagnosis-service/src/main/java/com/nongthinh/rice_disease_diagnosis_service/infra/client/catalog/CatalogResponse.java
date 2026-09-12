package com.nongthinh.rice_disease_diagnosis_service.infra.client.catalog;

public record CatalogResponse<T>(String code, String message, T result) {
}
