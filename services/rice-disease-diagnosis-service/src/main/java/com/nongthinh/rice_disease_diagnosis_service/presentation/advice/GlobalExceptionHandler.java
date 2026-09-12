package com.nongthinh.rice_disease_diagnosis_service.presentation.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorType;
import com.nongthinh.rice_disease_diagnosis_service.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DiagnosisException.class)
    public ResponseEntity<ApiResponse<Void>> handleDiagnosisException(DiagnosisException exception) {
        return response(exception.getErrorCode());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception ignored) {
        return response(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ignored) {
        return response(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error(
                "Unhandled diagnosis-service request error: exceptionType={}",
                exception.getClass().getName(),
                exception);
        return response(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> response(ErrorCode errorCode) {
        return ResponseEntity.status(status(errorCode.getErrorType()))
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getDefaultMessage()));
    }

    private HttpStatus status(ErrorType type) {
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
