package com.nongthinh.auth_service.presentation.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.nongthinh.auth_service.common.exception.AppException;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
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
import org.springframework.web.bind.annotation.*;

class GlobalExceptionHandlerTest {
    private final TestController controller = new TestController();
    private final GlobalExceptionHandler advice = new GlobalExceptionHandler(() -> Optional.of("test-trace"));
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
                Arguments.of(new InfrastructureException(ErrorCode.KEYCLOAK_USER_CREATION_FAILED),
                        500, ErrorCode.KEYCLOAK_USER_CREATION_FAILED, "InfrastructureException", Level.ERROR),
                Arguments.of(new InfrastructureException(ErrorCode.INVALID_TOKEN),
                        401, ErrorCode.INVALID_TOKEN, "InfrastructureException", Level.WARN),
                Arguments.of(new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS),
                        400, ErrorCode.EMAIL_ALREADY_EXISTS, "BusinessException", Level.WARN),
                Arguments.of(new BusinessException(ErrorCode.USER_NOT_FOUND),
                        404, ErrorCode.USER_NOT_FOUND, "BusinessException", Level.WARN),
                Arguments.of(new AppException(ErrorCode.INTERNAL_ERROR) {},
                        500, ErrorCode.INTERNAL_ERROR, "AppException", Level.ERROR),
                Arguments.of(new AccessDeniedException("Forbidden"),
                        403, ErrorCode.FORBIDDEN, "AccessDeniedException", Level.WARN),
                Arguments.of(new BadCredentialsException("private authentication details"),
                        401, ErrorCode.UNAUTHENTICATED, "AuthenticationException", Level.WARN),
                Arguments.of(new IllegalStateException("private system details"),
                        500, ErrorCode.INTERNAL_ERROR, "UnexpectedException", Level.ERROR));
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void dispatchesExceptionsWithExpectedStatusTraceAndLog(Exception exception, int status,
            ErrorCode errorCode, String handler, Level level) throws Exception {
        controller.failure = exception;

        mvc.perform(get("/failure"))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(1, logs.list.size());
        assertEquals(level, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().startsWith("[Presentation - " + handler + "]"));
        if (status >= 500) {
            assertNotNull(logs.list.getFirst().getThrowableProxy());
        }
    }

    @Test
    void feignFailureReturnsBadGatewayWithoutExposingUpstreamBody() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "http://upstream/profile", Map.of(),
                null, StandardCharsets.UTF_8, null);
        controller.failure = FeignException.errorStatus("ProfileClient#getBrandProfile",
                Response.builder().request(request).status(401).reason("Unauthorized")
                        .body("secret-upstream-token", StandardCharsets.UTF_8).build());

        mvc.perform(get("/failure"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("Internal error"))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(Level.ERROR, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().contains("upstreamStatus=401"));
        assertFalse(logs.list.getFirst().getFormattedMessage().contains("secret-upstream-token"));
        assertNull(logs.list.getFirst().getThrowableProxy());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{\"id\":\"invalid-uuid\"}"})
    void missingMalformedOrWrongTypeBodyReturnsBadRequest(String body) throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_KEY.getCode()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/parameter", "/parameter?id=invalid-uuid"})
    void missingOrInvalidParameterReturnsBadRequest(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_KEY.getCode()));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void beanValidationInterpolatesConstraintAttributes() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brandName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BRAND_NAME_LENGTH_INVALID.getCode()))
                .andExpect(jsonPath("$.message").value("Brand name must be between 1 and 200 characters"))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void constraintViolationPreservesValidationCodeAndAttributes() throws Exception {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            controller.failure = new ConstraintViolationException(
                    factory.getValidator().validate(new TestRequest("", null)));
            mvc.perform(get("/failure"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.BRAND_NAME_LENGTH_INVALID.getCode()))
                    .andExpect(jsonPath("$.message").value("Brand name must be between 1 and 200 characters"));
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"unknown-key", "PASSWORD_REQUIRED"})
    void objectValidationWithoutFieldOrConstraintSourceDoesNotFail(String key) throws Exception {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new ObjectError("request", key));
        controller.failure = new MethodArgumentNotValidException(new MethodParameter(
                TestController.class.getMethod("validate", TestRequest.class), 0), binding);

        mvc.perform(get("/failure"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_REQUIRED".equals(key)
                        ? ErrorCode.PASSWORD_REQUIRED.getCode() : ErrorCode.INVALID_KEY.getCode()));
    }

    @Test
    void emptyConstraintViolationsAndAbsentTraceHaveSafeFallback() {
        var response = new GlobalExceptionHandler(Optional::empty)
                .handleConstraintViolationException(new ConstraintViolationException(Set.of()));
        assertEquals(400, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_KEY.getCode(), response.getBody().getCode());
        assertNull(response.getBody().getTraceId());
    }

    record TestRequest(@Size(min = 1, max = 200, message = "BRAND_NAME_LENGTH_INVALID") String brandName,
                       UUID id) {}

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
