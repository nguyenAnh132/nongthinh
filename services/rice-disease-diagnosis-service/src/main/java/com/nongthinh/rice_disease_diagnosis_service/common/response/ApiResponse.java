package com.nongthinh.rice_disease_diagnosis_service.common.response;

public record ApiResponse<T>(
        String code,
        String message,
        String traceId,
        T result
) {
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(null, message, null, result);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return new ApiResponse<>(code, message, traceId, null);
    }
}
