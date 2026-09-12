package com.nongthinh.auth_service.presentation.advice;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.nongthinh.auth_service.common.exception.AppException;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.common.exception.ErrorType;
import com.nongthinh.auth_service.common.response.ApiResponse;
import com.nongthinh.auth_service.common.trace.TraceContextProvider;
import com.nongthinh.auth_service.domain.exception.BusinessException;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";
    private static final String MAX_ATTRIBUTE = "max";
    private static final String TYPE_ATTRIBUTE = "type";

    private final TraceContextProvider traceContextProvider;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(ex.getMessage())
                        .traceId(traceContextProvider.currentTraceId().orElse(null))
                        .result(null)
                        .build());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(ex.getMessage())
                        .traceId(traceContextProvider.currentTraceId().orElse(null))
                        .result(null)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getDefaultMessage())
                        .traceId(traceContextProvider.currentTraceId().orElse(null))
                        .result(null)
                        .build());
    }

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("Business exception: {}", ex.getMessage());
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(ex.getMessage())
                        .traceId(traceContextProvider.currentTraceId().orElse(null))
                        .result(null)
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info("attributes: {}", attributes);

        } catch (IllegalArgumentException e) {

        }

        ApiResponse<Void> apiResponse = new ApiResponse<>();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getDefaultMessage(), attributes)
                        : errorCode.getDefaultMessage());

        apiResponse.setTraceId(traceContextProvider.currentTraceId().orElse(null));
        return ResponseEntity.status(
            toHttpStatus(errorCode.getErrorType())
            ).body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        String maxValue = String.valueOf(attributes.get(MAX_ATTRIBUTE));
        String typeValue = String.valueOf(attributes.get(TYPE_ATTRIBUTE));

        return message
                .replace("{" + MIN_ATTRIBUTE + "}", minValue)
                .replace("{" + MAX_ATTRIBUTE + "}", maxValue)
                .replace("{" + TYPE_ATTRIBUTE + "}", typeValue);
    }

    private static HttpStatus toHttpStatus(ErrorType errorType) {
        return switch (errorType) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS_RULE -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case INTERNAL_SERVICE -> HttpStatus.BAD_GATEWAY;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
            case INFRASTRUCTURE -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
