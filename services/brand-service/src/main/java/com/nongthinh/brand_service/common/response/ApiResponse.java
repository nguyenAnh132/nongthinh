package com.nongthinh.brand_service.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ApiResponse<T> {
    @Builder.Default
    private String code = "1000";
    private String message;
    private String traceId;
    private T result;
}
