package com.nongthinh.rice_disease_diagnosis_service.infra.client.fileservice;

public record FileServiceResponse<T>(String code, String message, T result) {
}
