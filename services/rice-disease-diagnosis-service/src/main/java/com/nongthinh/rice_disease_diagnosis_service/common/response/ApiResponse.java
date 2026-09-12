package com.nongthinh.rice_disease_diagnosis_service.common.response;

public record ApiResponse<T>(
        String code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(null, message, result);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
