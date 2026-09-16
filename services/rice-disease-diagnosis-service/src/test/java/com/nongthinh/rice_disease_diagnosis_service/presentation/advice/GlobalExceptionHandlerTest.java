package com.nongthinh.rice_disease_diagnosis_service.presentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
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
                Arguments.of(new DiagnosisException(ErrorCode.DIAGNOSIS_HISTORY_NOT_FOUND), 404,
                        ErrorCode.DIAGNOSIS_HISTORY_NOT_FOUND, "DiagnosisException", Level.WARN),
                Arguments.of(new DiagnosisException(ErrorCode.FILE_SERVICE_UNAVAILABLE), 502,
                        ErrorCode.FILE_SERVICE_UNAVAILABLE, "DiagnosisException", Level.ERROR),
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
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REQUEST.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void beanValidationReturnsSafeInvalidRequest(String body) throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REQUEST.getDefaultMessage()))
                .andExpect(jsonPath("$.traceId").value("test-trace"));
        assertEquals(Level.WARN, logs.list.getFirst().getLevel());
    }

    static Stream<String> invalidBodies() {
        return Stream.of("{}", "{\"name\":\"\"}");
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

    record TestRequest(@NotBlank String name) {
    }
}
