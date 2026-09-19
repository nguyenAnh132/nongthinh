package com.nongthinh.auth_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import com.nongthinh.auth_service.application.exception.RegistrationRequiredException;
import com.nongthinh.auth_service.application.port.in.user.CompleteRegistrationUseCase;
import com.nongthinh.auth_service.application.view.CompleteRegistrationView;
import com.nongthinh.auth_service.common.trace.TraceContextProvider;
import com.nongthinh.auth_service.infra.CurrentUserProviderImpl;
import com.nongthinh.auth_service.presentation.advice.GlobalExceptionHandler;
import com.nongthinh.auth_service.presentation.controller.RegistrationController;
import com.nongthinh.auth_service.presentation.dto.request.CompleteRegistrationRequest;
import com.nongthinh.auth_service.presentation.mapper.RegistrationMapperImpl;

class CompleteRegistrationControllerTest {
    private final CompleteRegistrationUseCase useCase = mock(CompleteRegistrationUseCase.class);

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void requestCannotChooseAnotherIdentityOrElevateFarmerToAdmin() throws Exception {
        authenticate("ROLE_FARMER");
        try (var context = context()) {
            when(useCase.execute(any(), any())).thenReturn(new CompleteRegistrationView(UUID.randomUUID(), true));
            var mvc = MockMvcBuilders.standaloneSetup(context.getBean(RegistrationController.class)).build();
            mvc.perform(post("/me/complete-registration").contentType(MediaType.APPLICATION_JSON).content("""
                    {"firstName":"An","lastName":"Nguyen Van","gender":"OTHER","phone":"0900000000",
                     "role":"ROLE_ADMIN","adminGroup":"SUPER_ADMIN","userId":"another-user"}
                    """))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.result.refreshRequired").value(true));
            verify(useCase).execute(argThat(p -> p.role().equals("ROLE_FARMER")
                    && p.keycloakId().toString().endsWith("000000000002") && p.applicationUserId() == null), any());
        }
    }

    @Test
    void authenticatedCallerWithoutApplicationRoleIsDenied() {
        authenticate("unrelated-role");
        try (var context = context()) {
            var request = new CompleteRegistrationRequest("An", "Nguyen Van", "OTHER", "0900000000", null, null, null, null);
            assertThrows(AccessDeniedException.class, () -> context.getBean(RegistrationController.class).complete(request));
            verifyNoInteractions(useCase);
        }
    }

    @Test
    void missingProfileUsesDedicated409EnvelopeWithRoleContext() throws Exception {
        var trace = mock(TraceContextProvider.class);
        when(trace.currentTraceId()).thenReturn(Optional.empty());
        var mvc = MockMvcBuilders.standaloneSetup(new MissingProfileController())
                .setControllerAdvice(new GlobalExceptionHandler(trace)).build();
        mvc.perform(get("/me")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_REGISTRATION_REQUIRED"))
                .andExpect(jsonPath("$.result.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.result.email").value("admin@example.com"));
    }

    private AnnotationConfigApplicationContext context() {
        var context = new AnnotationConfigApplicationContext();
        context.register(MethodSecurity.class);
        context.registerBean(RegistrationController.class, () -> new RegistrationController(useCase, new CurrentUserProviderImpl(), new RegistrationMapperImpl()));
        context.refresh();
        return context;
    }

    private void authenticate(String role) {
        var jwt = Jwt.withTokenValue("test").header("alg", "RS256")
                .subject("00000000-0000-0000-0000-000000000002").claim("email", "user@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role))));
    }

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurity { }

    @RestController
    static class MissingProfileController {
        @GetMapping("/me")
        public void get() { throw new RegistrationRequiredException("admin@example.com", "ROLE_ADMIN"); }
    }
}
