package com.nongthinh.post_service.infra.client.fileservice;

public record FileServiceResponse<T>(String code, String message, T result) {
}
