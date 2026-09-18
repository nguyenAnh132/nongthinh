package com.nongthinh.rice_disease_diagnosis_service.presentation.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.CreateDiagnosisUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.GetDiagnosisHistoryUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.ListDiagnosisHistoriesUseCase;
import com.nongthinh.rice_disease_diagnosis_service.presentation.dto.request.DiagnosisRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

class DiagnosisAuthorizationTest {
    private final CreateDiagnosisUseCase create = mock(CreateDiagnosisUseCase.class);
    private final ListDiagnosisHistoriesUseCase list = mock(ListDiagnosisHistoriesUseCase.class);
    private final GetDiagnosisHistoryUseCase detail = mock(GetDiagnosisHistoryUseCase.class);
    private AnnotationConfigApplicationContext context;
    private DiagnosisController controller;
    private final UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Configuration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration { }

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext();
        context.register(MethodSecurityConfiguration.class);
        context.registerBean(CreateDiagnosisUseCase.class, () -> create);
        context.registerBean(ListDiagnosisHistoriesUseCase.class, () -> list);
        context.registerBean(GetDiagnosisHistoryUseCase.class, () -> detail);
        context.registerBean(DiagnosisController.class);
        context.refresh();
        controller = context.getBean(DiagnosisController.class);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_FARMER", "ROLE_BRAND"})
    void permitsDiagnosisAndBothHistoryEndpoints(String role) {
        authenticate(role);
        controller.diagnose(new DiagnosisRequest(id, List.of(id)));
        controller.history();
        controller.historyDetail(id);
        verify(create).execute(argThat(command -> command.cropTypeId().equals(id) && command.fileIds().equals(List.of(id))));
        verify(list).execute();
        verify(detail).execute(id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMIN", "ROLE_UNKNOWN"})
    void rejectsOtherRolesBeforeCallingUseCases(String role) {
        authenticate(role);
        assertThatThrownBy(() -> controller.diagnose(new DiagnosisRequest(id, List.of(id))))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.history()).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> controller.historyDetail(id)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(create, list, detail);
    }

    @Test
    void rejectsUnauthenticatedAccess() {
        assertThatThrownBy(() -> controller.history()).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
        verifyNoInteractions(create, list, detail);
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user", "unused", List.of(new SimpleGrantedAuthority(role))));
    }
}
