package com.nongthinh.notification_service.presentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.nongthinh.notification_service.common.exception.AppException;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import com.nongthinh.notification_service.infra.exception.InfrastructureException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.server.ResponseStatusException;

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
                Arguments.of(new InfrastructureException(ErrorCode.EVENT_DESERIALIZATION_FAILED), 500,
                        ErrorCode.EVENT_DESERIALIZATION_FAILED, "InfrastructureException", Level.ERROR),
                Arguments.of(new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND), 404,
                        ErrorCode.NOTIFICATION_NOT_FOUND, "BusinessException", Level.WARN),
                Arguments.of(new AppException(ErrorCode.INTERNAL_ERROR) {}, 500,
                        ErrorCode.INTERNAL_ERROR, "AppException", Level.ERROR),
                Arguments.of(new AccessDeniedException("private details"), 403,
                        ErrorCode.FORBIDDEN, "AccessDeniedException", Level.WARN),
                Arguments.of(new BadCredentialsException("private details"), 401,
                        ErrorCode.UNAUTHENTICATED, "AuthenticationException", Level.WARN),
                Arguments.of(new IllegalStateException("private details"), 500,
                        ErrorCode.INTERNAL_ERROR, "UnexpectedException", Level.ERROR));
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void dispatchesExceptionsWithExpectedEnvelopeAndLog(Exception exception, int expectedStatus,
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
    void responseStatusPreservesStatusWithoutExposingReason() throws Exception {
        controller.failure = new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "private limit details");

        mvc.perform(get("/failure"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("HTTP_429"))
                .andExpect(jsonPath("$.message").value("Request cannot be completed"))
                .andExpect(jsonPath("$.traceId").value("test-trace"));

        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
        assertTrue(logs.list.getFirst().getFormattedMessage().contains("status=429"));
        assertNull(logs.list.getFirst().getThrowableProxy());
    }

    @Test
    void disconnectedSseStreamIsLoggedAtDebugWithoutEnvelope() {
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            advice.handleDisconnectedStream(new AsyncRequestNotUsableException("client disconnected"));

            assertEquals(1, logs.list.size());
            assertEquals(Level.DEBUG, logs.list.getFirst().getLevel());
            assertTrue(logs.list.getFirst().getFormattedMessage().contains("SSE client disconnected"));
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of("/parameter", false),
                Arguments.of("/parameter?id=invalid-uuid", false),
                Arguments.of("/validate", true));
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void invalidInputReturnsBadRequest(String path, boolean postRequest) throws Exception {
        var request = postRequest
                ? post(path).contentType(MediaType.APPLICATION_JSON).content("{")
                : get(path);
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOTIFICATION_REQUEST_INVALID.getCode()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @Test
    void beanValidationUsesConfiguredErrorCode() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.TEMPLATE_NAME_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TEMPLATE_NAME_REQUIRED.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @RestController
    static class TestController {
        Exception failure;

        @GetMapping("/failure")
        void failure() throws Exception {
            throw failure;
        }

        @GetMapping("/parameter")
        void parameter(@RequestParam UUID id) {
        }

        @PostMapping("/validate")
        void validate(@RequestBody @Valid TestRequest request) {
        }
    }

    record TestRequest(@NotBlank(message = "TEMPLATE_NAME_REQUIRED") String name) {
    }
}
