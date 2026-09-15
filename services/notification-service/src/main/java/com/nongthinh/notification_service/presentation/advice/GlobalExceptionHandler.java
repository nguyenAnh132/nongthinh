package com.nongthinh.notification_service.presentation.advice;

import java.util.Map;
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
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import com.nongthinh.notification_service.common.exception.AppException;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.common.exception.ErrorType;
import com.nongthinh.notification_service.common.response.ApiResponse;
import com.nongthinh.notification_service.common.trace.TraceContextProvider;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import com.nongthinh.notification_service.infra.exception.InfrastructureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";
    private static final String MAX_ATTRIBUTE = "max";
    private static final String TYPE_ATTRIBUTE = "type";

    private final TraceContextProvider traceContextProvider;

    public GlobalExceptionHandler(Optional<TraceContextProvider> traceContextProvider) {
        this.traceContextProvider = traceContextProvider.orElse(Optional::empty);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        logAppException("AppException", ex);
        return error(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        logAppException("BusinessException", ex);
        return error(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInfrastructureException(InfrastructureException ex) {
        logAppException("InfrastructureException", ex);
        return error(ex.getErrorCode(), ex.getMessage());
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

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(Exception ex) {
        ErrorCode errorCode = ErrorCode.NOTIFICATION_REQUEST_INVALID;
        log.warn("[Presentation - InvalidRequest] Invalid request | code={} exception={}",
                errorCode.getCode(), ex.getClass().getSimpleName());
        return error(errorCode, errorCode.getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        var violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        ErrorCode errorCode = resolveErrorCode(violation == null ? null : violation.getMessage());
        String message = violation == null ? errorCode.getDefaultMessage()
                : mapAttribute(errorCode.getDefaultMessage(), violation.getConstraintDescriptor().getAttributes());
        log.warn("[Presentation - ConstraintViolationException] Validation failed | code={} message={} violationCount={}",
                errorCode.getCode(), message, ex.getConstraintViolations().size());
        return error(errorCode, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        var bindingResult = ex.getBindingResult();
        var validationError = bindingResult.getFieldError() != null
                ? bindingResult.getFieldError() : bindingResult.getGlobalError();
        ErrorCode errorCode = resolveErrorCode(validationError == null ? null : validationError.getDefaultMessage());
        String message = errorCode.getDefaultMessage();
        if (validationError != null && validationError.contains(ConstraintViolation.class)) {
            ConstraintViolation<?> violation = validationError.unwrap(ConstraintViolation.class);
            message = mapAttribute(message, violation.getConstraintDescriptor().getAttributes());
        }

        log.warn("[Presentation - MethodArgumentNotValidException] Validation failed | code={} message={} errorCount={}",
                errorCode.getCode(), message, bindingResult.getErrorCount());
        return error(errorCode, message);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnectedStream(AsyncRequestNotUsableException ex) {
        log.debug("[Presentation - AsyncRequestNotUsableException] SSE client disconnected | exception={}",
                ex.getClass().getSimpleName());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        String code = "HTTP_" + ex.getStatusCode().value();
        if (ex.getStatusCode().is5xxServerError()) {
            log.error("[Presentation - ResponseStatusException] Request failed | code={} status={}",
                    code, ex.getStatusCode().value(), ex);
        } else {
            log.warn("[Presentation - ResponseStatusException] Request rejected | code={} status={}",
                    code, ex.getStatusCode().value());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.<Void>builder()
                .code(code)
                .message("Request cannot be completed")
                .traceId(traceContextProvider.currentTraceId().orElse(null))
                .result(null)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        log.error("[Presentation - UnexpectedException] Unexpected error | code={}", errorCode.getCode(), ex);
        return error(errorCode, errorCode.getDefaultMessage());
    }

    private ErrorCode resolveErrorCode(String key) {
        if (key == null) {
            return ErrorCode.INVALID_KEY;
        }
        try {
            return ErrorCode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return ErrorCode.INVALID_KEY;
        }
    }

    private void logAppException(String handler, AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = toHttpStatus(errorCode.getErrorType());
        if (status.is5xxServerError()) {
            log.error("[Presentation - {}] Request failed | code={} status={} message={}",
                    handler, errorCode.getCode(), status.value(), ex.getMessage(), ex);
        } else {
            log.warn("[Presentation - {}] Request rejected | code={} status={} message={}",
                    handler, errorCode.getCode(), status.value(), ex.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        return ResponseEntity.status(toHttpStatus(errorCode.getErrorType()))
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .traceId(traceContextProvider.currentTraceId().orElse(null))
                        .result(null)
                        .build());
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        return message
                .replace("{" + MIN_ATTRIBUTE + "}", String.valueOf(attributes.get(MIN_ATTRIBUTE)))
                .replace("{" + MAX_ATTRIBUTE + "}", String.valueOf(attributes.get(MAX_ATTRIBUTE)))
                .replace("{" + TYPE_ATTRIBUTE + "}", String.valueOf(attributes.get(TYPE_ATTRIBUTE)));
    }

    private static HttpStatus toHttpStatus(ErrorType errorType) {
        return switch (errorType) {
            case VALIDATION, BUSINESS_RULE -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case INTERNAL_SERVICE -> HttpStatus.BAD_GATEWAY;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
