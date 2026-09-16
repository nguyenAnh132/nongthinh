package com.nongthinh.rice_disease_diagnosis_service.presentation.advice;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorType;
import com.nongthinh.rice_disease_diagnosis_service.common.response.ApiResponse;
import com.nongthinh.rice_disease_diagnosis_service.common.trace.TraceContextProvider;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final TraceContextProvider traceContextProvider;

    public GlobalExceptionHandler(Optional<TraceContextProvider> traceContextProvider) {
        this.traceContextProvider = traceContextProvider.orElse(Optional::empty);
    }

    @ExceptionHandler(DiagnosisException.class)
    public ResponseEntity<ApiResponse<Void>> handleDiagnosisException(DiagnosisException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        logDiagnosisException(ex);
        return error(errorCode, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        log.warn("[Presentation - AccessDeniedException] Access denied | code={} exception={}",
                errorCode.getCode(), ex.getClass().getSimpleName());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        log.warn("[Presentation - AuthenticationException] Authentication failed | code={} exception={}",
                errorCode.getCode(), ex.getClass().getSimpleName());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        // Feign messages can contain upstream response bodies, including credentials.
        log.error("[Presentation - FeignException] Upstream request failed | code={} upstreamStatus={} exception={}",
                errorCode.getCode(), ex.status(), ex.getClass().getSimpleName());
        return error(HttpStatus.BAD_GATEWAY, errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception ex) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        log.warn("[Presentation - InvalidRequest] Invalid request | code={} exception={}",
                errorCode.getCode(), ex.getClass().getSimpleName());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        log.warn("[Presentation - ConstraintViolationException] Validation failed | code={} violationCount={}",
                errorCode.getCode(), ex.getConstraintViolations().size());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        log.warn("[Presentation - MethodArgumentNotValidException] Validation failed | code={} errorCount={}",
                errorCode.getCode(), ex.getBindingResult().getErrorCount());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        log.error("[Presentation - UnexpectedException] Unexpected error | code={}", errorCode.getCode(), ex);
        return error(errorCode, errorCode.getDefaultMessage());
    }

    private void logDiagnosisException(DiagnosisException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = status(errorCode.getErrorType());
        if (status.is5xxServerError()) {
            log.error("[Presentation - DiagnosisException] Request failed | code={} status={} message={}",
                    errorCode.getCode(), status.value(), ex.getMessage(), ex);
        } else {
            log.warn("[Presentation - DiagnosisException] Request rejected | code={} status={} message={}",
                    errorCode.getCode(), status.value(), ex.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        return error(status(errorCode.getErrorType()), errorCode, message);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, ErrorCode errorCode, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(
                        errorCode.getCode(),
                        message,
                        traceContextProvider.currentTraceId().orElse(null)));
    }

    private static HttpStatus status(ErrorType type) {
        return switch (type) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case INFRASTRUCTURE -> HttpStatus.BAD_GATEWAY;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
