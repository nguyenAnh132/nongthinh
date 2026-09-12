package com.nongthinh.agri_catalog_service.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private String traceId;
    private T result;
}
