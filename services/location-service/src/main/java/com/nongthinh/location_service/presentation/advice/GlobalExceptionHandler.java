package com.nongthinh.location_service.presentation.advice;

import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.nongthinh.location_service.common.exception.AppException;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.common.exception.ErrorType;
import com.nongthinh.location_service.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(ex.getMessage())
                        .result(Optional.empty())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(ex.getMessage())
                        .result(Optional.empty())
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
                        .result(Optional.empty())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException | NullPointerException e) {
            errorCode = ErrorCode.INVALID_KEY;
        }

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .result(Optional.empty())
                .build();

        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType())).body(apiResponse);
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
        };
    }
}
