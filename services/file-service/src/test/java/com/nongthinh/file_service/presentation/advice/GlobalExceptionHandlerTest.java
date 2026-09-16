package com.nongthinh.file_service.presentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.nongthinh.file_service.common.exception.AppException;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import feign.FeignException;
import feign.Request;
import feign.Response;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

class GlobalExceptionHandlerTest {
    private final TestController controller = new TestController();
    private final GlobalExceptionHandler advice = new GlobalExceptionHandler(
            Optional.of(() -> Optional.of("test-trace")));
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        logs.start();
        logger.addAppender(logs);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(advice).build();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logs);
        logs.stop();
    }

    static Stream<Arguments> exceptions() {
        return Stream.of(
                Arguments.of(new BusinessException(ErrorCode.FILE_ALREADY_DELETED),
                        400, ErrorCode.FILE_ALREADY_DELETED, "BusinessException", Level.WARN),
                Arguments.of(new BusinessException(ErrorCode.FILE_NOT_FOUND),
                        404, ErrorCode.FILE_NOT_FOUND, "BusinessException", Level.WARN),
                Arguments.of(new BusinessException(ErrorCode.STORAGE_UNAVAILABLE),
                        502, ErrorCode.STORAGE_UNAVAILABLE, "BusinessException", Level.ERROR),
                Arguments.of(new AppException(ErrorCode.INTERNAL_ERROR) {},
                        500, ErrorCode.INTERNAL_ERROR, "AppException", Level.ERROR),
                Arguments.of(new AccessDeniedException("private authorization details"),
                        403, ErrorCode.FORBIDDEN, "AccessDeniedException", Level.WARN),
                Arguments.of(new BadCredentialsException("private authentication details"),
                        401, ErrorCode.UNAUTHENTICATED, "AuthenticationException", Level.WARN),
                Arguments.of(new IllegalStateException("private system details"),
                        500, ErrorCode.INTERNAL_ERROR, "UnexpectedException", Level.ERROR));
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void dispatchesExceptionsWithExpectedStatusTraceAndLog(Exception exception, int expectedStatus,
            ErrorCode errorCode, String handler, Level level) throws Exception {
        controller.failure = exception;

        mvc.perform(get("/failure"))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(1, logs.list.size());
        assertEquals(level, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().startsWith("[Presentation - " + handler + "]"));
        if (expectedStatus >= 500) {
            assertNotNull(logs.list.getFirst().getThrowableProxy());
        }
    }

    @Test
    void feignFailureReturnsBadGatewayWithoutExposingUpstreamBody() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "http://upstream/file", Map.of(),
                null, StandardCharsets.UTF_8, null);
        controller.failure = FeignException.errorStatus("FileClient#getFile",
                Response.builder().request(request).status(401).reason("Unauthorized")
                        .body("secret-upstream-credential", StandardCharsets.UTF_8).build());

        mvc.perform(get("/failure"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_ERROR.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(Level.ERROR, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().contains("upstreamStatus=401"));
        assertFalse(logs.list.getFirst().getFormattedMessage().contains("secret-upstream-credential"));
        assertNull(logs.list.getFirst().getThrowableProxy());
    }

    @Test
    void oversizedUploadReturnsFileTooLarge() throws Exception {
        controller.failure = new MaxUploadSizeExceededException(1024);

        mvc.perform(get("/failure"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.FILE_TOO_LARGE.getCode()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().contains("maxUploadSize=1024"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{\"id\":\"invalid-uuid\"}"})
    void missingMalformedOrWrongTypeBodyReturnsBadRequest(String body) throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/parameter", "/parameter?id=invalid-uuid"})
    void missingOrInvalidParameterReturnsBadRequest(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void missingMultipartPartUsesValidationError() throws Exception {
        controller.failure = new MissingServletRequestPartException("file");

        mvc.perform(get("/failure"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void beanValidationPreservesConfiguredErrorCode() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purpose\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.PURPOSE_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PURPOSE_INVALID.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void constraintViolationPreservesConfiguredErrorCode() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            controller.failure = new ConstraintViolationException(
                    factory.getValidator().validate(new TestRequest("", null)));
            mvc.perform(get("/failure"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.PURPOSE_INVALID.getCode()));
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"unknown-key", "PURPOSE_REQUIRED"})
    void objectValidationWithoutFieldOrConstraintSourceDoesNotFail(String key) throws Exception {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new ObjectError("request", key));
        controller.failure = new MethodArgumentNotValidException(new MethodParameter(
                TestController.class.getMethod("validate", TestRequest.class), 0), binding);

        mvc.perform(get("/failure"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PURPOSE_REQUIRED".equals(key)
                        ? ErrorCode.PURPOSE_REQUIRED.getCode() : ErrorCode.INVALID_KEY.getCode()));
    }

    @Test
    void emptyConstraintViolationsAndAbsentTraceHaveSafeFallback() {
        var response = new GlobalExceptionHandler(Optional.empty())
                .handleConstraintViolationException(new ConstraintViolationException(Set.of()));
        assertEquals(400, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_KEY.getCode(), response.getBody().getCode());
        assertNull(response.getBody().getTraceId());
    }

    record TestRequest(@Size(min = 1, message = "PURPOSE_INVALID") String purpose, UUID id) {}

    @RestController
    static class TestController {
        Exception failure;

        @GetMapping("/failure")
        public void fail() throws Exception {
            throw failure;
        }

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {}

        @GetMapping("/parameter")
        public void parameter(@RequestParam("id") UUID id) {}
    }
}
