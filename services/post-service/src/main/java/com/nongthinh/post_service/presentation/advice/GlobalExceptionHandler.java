package com.nongthinh.post_service.presentation.advice;

import com.nongthinh.post_service.common.exception.AppException;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.common.exception.ErrorType;
import com.nongthinh.post_service.common.response.ApiResponse;
import java.util.Objects;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return error(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException() {
        return error(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String key = Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();
        try {
            ErrorCode errorCode = ErrorCode.valueOf(key);
            return error(errorCode, errorCode.getDefaultMessage());
        } catch (IllegalArgumentException exception) {
            return error(ErrorCode.INVALID_REQUEST_PARAMETER, ErrorCode.INVALID_REQUEST_PARAMETER.getDefaultMessage());
        }
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestValue() {
        return error(ErrorCode.INVALID_REQUEST_PARAMETER, ErrorCode.INVALID_REQUEST_PARAMETER.getDefaultMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidArgument() {
        return error(ErrorCode.INVALID_REQUEST_PARAMETER,
                ErrorCode.INVALID_REQUEST_PARAMETER.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);
        return error(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder().code(errorCode.getCode()).message(message).build());
    }

    private static HttpStatus toHttpStatus(ErrorType errorType) {
        return switch (errorType) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS_RULE -> HttpStatus.CONFLICT;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case INFRASTRUCTURE -> HttpStatus.SERVICE_UNAVAILABLE;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
